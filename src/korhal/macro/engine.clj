(ns korhal.macro.engine
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.macro.build-order :refer [get-build-order get-random-build-order]]
            [korhal.tools.contract :refer [available-minerals available-gas
                                           contract-build contract-train
                                           contracted-max-supply
                                           clear-contracts cancel-contracts
                                           show-contract-display clear-contract-atoms
                                           can-build?]]))

(def macro-state (ref {:build-order []}))

(defn start-macro-engine []
  (dosync
   (commute macro-state assoc-in [:build-order] (get-random-build-order))))

(defn- mine-with-idle-scvs []
  (doseq [idle-scv (filter idle? (my-scvs))]
    (let [closest-mineral (apply min-key (partial dist idle-scv) (minerals))]
      (cancel-contracts idle-scv)
      (right-click idle-scv closest-mineral))))

(defn- maybe-train-scvs
  "Train SCVs if not already at maximum for number of expansions.
  NOTE: Doesn't actually do this yet. :)"
  []
  (doseq [cc (filter #(zero? (training-queue-size %)) (my-command-centers))]
    (when (>= (available-minerals) 50)
      (contract-train cc :scv))))

(defn- find-build-location [build-kw]
  (let [cc (first (my-command-centers))]
    (loop [attempt 0]
      (when-not (>= attempt 5)
        (let [tx (+ (tile-x cc) (* (Math/pow -1 (rand-int 2)) (rand-int 20)))
              ty (+ (tile-y cc) (* (Math/pow -1 (rand-int 2)) (rand-int 20)))]
          (if (can-build? tx ty build-kw true)
            [tx ty]
            (recur (inc attempt))))))))

(defn- ensure-enough-depots []
  (when (and (>= (+ (my-supply-used) 3) (contracted-max-supply))
             (>= (available-minerals) 100))
    (let [builder (first (filter gathering-minerals? (my-scvs)))]
      (when-let [[tx ty] (find-build-location :supply-depot)]
        (cancel-contracts builder)
        (contract-build builder tx ty :supply-depot)))))

(defn- pop-build-order [] nil)

(defn- expand [] nil)

(defn- build-kw [value] nil)

(defn- train-kw [value] nil)

(defn- research-kw [value] nil)

(defn- upgrade-kw [value] nil)

(defn- continuous-kw [value] nil)

(defn process-build-order-step []
  (let [[directive value] (first (partition 2 (:build-order @macro-state)))]
    (if (number? directive)
      (when (>= (my-supply-used) directive)
        (if (= value :wait)
          (pop-build-order)
          (if (= value :command-center) (expand) (build-kw value))))
      (condp = directive
        :train (train-kw value)
        :research (research-kw value)
        :upgrade (upgrade-kw value)
        :continuous (continuous-kw value)))))

(defn run-macro-engine
  "Issue commands based on the current state of the game and the macro
  engine. Should be called in each gameUpdate loop."
  []
  (mine-with-idle-scvs)
  (maybe-train-scvs)
  (if (seq (:build-order @macro-state))
    (process-build-order-step)
    (do (ensure-enough-depots))))
