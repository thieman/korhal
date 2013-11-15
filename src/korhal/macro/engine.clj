(ns korhal.macro.engine
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.macro.state :refer [macro-state macro-tag-unit! get-macro-tag pop-build-order!]]
            [korhal.macro.command :refer :all]
            [korhal.micro.engine :refer [micro-tag-unit! get-micro-tag]]
            [korhal.tools.contract :refer [cancel-contracts contract-train
                                           can-afford? contracted-max-supply]]))

(defn- send-early-game-scout []
  (when-let [scv (assign-spare-scv! nil)]
    (micro-tag-unit! scv :early-scout)
    (pop-build-order!)))

(defn- check-for-failed-builders [])

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

(defn process-build-order-step []
  (let [[directive kw] (first (partition 2 (:build-order @macro-state)))]
    (if (number? directive)
      (when (>= (my-supply-used) directive)
        (cond
         (= kw :wait) (pop-build-order!)
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
  (check-for-failed-builders)
  (mine-with-idle-scvs)
  (maybe-train-scvs)
  (if (seq (:build-order @macro-state))
    (process-build-order-step)
    (do (ensure-enough-depots))))
