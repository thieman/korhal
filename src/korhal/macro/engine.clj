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
   (commute macro-state assoc-in [:build-order] [])))

(defn- mine-with-idle-scvs []
  (doseq [idle-scv (filter idle? (my-scvs))]
    (let [closest-mineral (apply min-key (partial dist idle-scv) (minerals))]
      (cancel-contracts idle-scv)
      (right-click idle-scv closest-mineral))))

(defn- train-scvs []
  (doseq [cc (filter #(zero? (training-queue-size %)) (my-command-centers))]
    (when (>= (available-minerals) 50)
      (contract-train cc :scv))))

(defn- ensure-enough-depots []
  (when (and (>= (+ (my-supply-used) 200) (contracted-max-supply))
             (>= (available-minerals) 100))
    (let [cc (first (my-command-centers))
          builder (first (filter gathering-minerals? (my-scvs)))]
      (loop [attempt 0]
        (when-not (>= attempt 5)
          (let [tx (+ (tile-x cc) (* (Math/pow -1 (rand-int 2)) (rand-int 20)))
                ty (+ (tile-y cc) (* (Math/pow -1 (rand-int 2)) (rand-int 20)))]
            (if (can-build? tx ty :supply-depot true)
              (do (cancel-contracts builder)
                  (contract-build builder tx ty :supply-depot))
              (recur (inc attempt)))))))))

(defn run-macro-engine
  "Issue commands based on the current state of the game and the macro
  engine. Should be called in each gameUpdate loop."
  []
  (mine-with-idle-scvs)
  (if (seq (:build-order @macro-state))
    (do nil) ;; build order stuff
    (do (train-scvs)
        (ensure-enough-depots))))
