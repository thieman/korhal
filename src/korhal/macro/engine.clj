(ns korhal.macro.engine
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.macro.build-order :refer [build-orders get-random-build-order]]
            [korhal.micro.engine :refer [micro-tag-unit! get-micro-tag]]
            [korhal.tools.contract :refer [contract-build contract-train
                                           contract-upgrade contract-research
                                           contracted-max-supply
                                           clear-contracts cancel-contracts
                                           show-contract-display clear-contract-atoms
                                           can-build? can-afford?]])
  (:import (jnibwapi.model Unit)))

(def macro-state (ref {:build-order [] :tags {}}))

(defn start-macro-engine []
  (dosync
   (commute macro-state assoc-in [:build-order] (build-orders :test-order))
   (commute macro-state assoc-in [:tags] {})))

(defn macro-tag-unit! [unit-or-unit-id tag]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (dosync
     (commute macro-state assoc-in [:tags unit-id] tag))))

(defn get-macro-tag [unit-or-unit-id]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (get-in @macro-state [:tags unit-id])))

(defn- pop-build-order []
  (dosync
   (commute macro-state update-in [:build-order] nnext)))

(defn- assign-spare-scv!
  "Get an available SCV from a mineral line and assign it a macro tag."
  [tag]
  (let [available? (fn [scv] (= (get-macro-tag scv) :minerals))
        scv (first (filter available? (my-scvs)))]
    (macro-tag-unit! scv tag)
    scv))

(defn- assign-nearest-spare-scv!
  "Get an available SCV from a mineral line and assign it a macro tag."
  [building tag]
  (let [available? (fn [scv] (= (get-macro-tag scv) :minerals))
        available-scvs (filter available? (my-scvs))
        scv (apply min-key (partial dist-tile building) available-scvs)]
    (macro-tag-unit! scv tag)
    scv))

(defn- send-early-game-scout []
  (when-let [scv (assign-spare-scv! nil)]
    (micro-tag-unit! scv :early-scout)
    (pop-build-order)))

(defn- mine-with-idle-scvs []
  (doseq [idle-scv (filter idle? (my-scvs))]
    (cancel-contracts idle-scv)
    (macro-tag-unit! idle-scv :minerals)
    (micro-tag-unit! idle-scv :minerals)))

(defn- maybe-train-scvs
  "Train SCVs if not already at maximum for number of expansions.
  NOTE: Doesn't actually do this yet. :)"
  []
  (doseq [cc (filter #(zero? (training-queue-size %)) (my-command-centers))]
    (when (can-afford? :scv)
      (contract-train cc :scv))))

(defn- find-build-location [build-kw]
  (let [cc (first (my-command-centers))]
    (loop [attempt 0]
      (when-not (>= attempt 5)
        (let [tx (+ (tile-x cc) (* (Math/pow -1 (rand-int 2)) (rand-int 15)))
              ty (+ (tile-y cc) (* (Math/pow -1 (rand-int 2)) (rand-int 15)))]
          (if (can-build? tx ty build-kw true)
            [tx ty]
            (recur (inc attempt))))))))

(defn can-build-now? [b]
  (and (zero? (training-queue-size b))
       (completed? b)))

(defn- nearest-expansion []
  (let [not-my-base? (fn [base] (or (not= (.x (my-start-location)) (tile-x base))
                                    (not= (.y (my-start-location)) (tile-y base))))
        bases (filter not-my-base? (base-locations))
        nearest-base (apply min-key (partial dist-tile (first (my-command-centers))) bases)]
    (when nearest-base nearest-base)))

(defn- expand
  ([] (expand false))
  ([pop?]
     (let [expo (nearest-expansion)
           tx (tile-x expo)
           ty (tile-y expo)]
       (when (and tx ty (can-afford? :command-center))
         (let [builder (assign-nearest-spare-scv! expo :building)]
           (cancel-contracts builder)
           (contract-build builder tx ty :command-center)
           (when pop? (pop-build-order)))))))

(defn- build-refinery
  ([] (build-refinery false))
  ([pop?]
     (let [cc (first (my-command-centers))
           closest-geyser (apply min-key (partial dist cc) (geysers))]
       (when (and closest-geyser (can-afford? :refinery))
         (let [builder (assign-spare-scv! :building)]
           (cancel-contracts builder)
           (contract-build builder (tile-x closest-geyser) (tile-y closest-geyser) :refinery)
           (when pop? (pop-build-order)))))))

(defn- build-kw
  ([kw] (build-kw kw false))
  ([kw pop?]
     (when (can-afford? kw)
       (when-let [[tx ty] (find-build-location kw)]
         (let [builder (assign-spare-scv! :building)]
           (cancel-contracts builder)
           (contract-build builder tx ty kw)
           (when pop? (pop-build-order)))))))

(defn- train-kw [kw]
  (let [unit-type (get-unit-type (kw unit-type-kws))
        what-builds (what-build-id unit-type)
        my-builders (my-buildings-id what-builds)
        builder (first (filter can-build-now? my-builders))]
    (when (and builder (can-afford? unit-type))
      (contract-train builder kw)
      (pop-build-order))))

(defn- research-kw [kw]
  (let [tech-type (get-tech-type (kw tech-type-kws))
        what-builds (what-researches tech-type)
        my-builders (my-buildings-id (get-id what-builds))
        builder (first (filter can-build-now? my-builders))]
    (when (and builder (can-afford? tech-type))
      (contract-research builder kw)
      (pop-build-order))))

(defn- upgrade-kw [kw]
  (let [upgrade-type (get-upgrade-type (kw upgrade-type-kws))
        what-builds (what-upgrades upgrade-type)
        my-builders (my-buildings-id (get-id what-builds))
        builder (first (filter can-build-now? my-builders))]
    (when (and builder (can-afford? upgrade-type))
      (contract-upgrade builder kw)
      (pop-build-order))))

(defn process-build-order-step []
  (let [[directive kw] (first (partition 2 (:build-order @macro-state)))]
    (if (number? directive)
      (when (>= (my-supply-used) directive)
        (cond
         (= kw :wait) (pop-build-order)
         (= kw :send-scout) (send-early-game-scout)
         :else (condp = kw
                 :command-center (expand true)
                 :refinery (build-refinery true)
                 (build-kw kw true))))
      (when (can-afford? kw)
        (condp = directive
          :train (train-kw kw)
          :research (research-kw kw)
          :upgrade (upgrade-kw kw))))))

(defn- ensure-enough-depots []
  (when (and (>= (+ (my-supply-used) 3) (contracted-max-supply))
             (can-afford? :supply-depot))
    (build-kw :supply-depot)))

(defn run-macro-engine
  "Issue commands based on the current state of the game and the macro
  engine. Should be called in each gameUpdate loop."
  []
  (mine-with-idle-scvs)
  (maybe-train-scvs)
  (if (seq (:build-order @macro-state))
    (process-build-order-step)
    (do (ensure-enough-depots))))
