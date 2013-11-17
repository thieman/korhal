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
    (micro-tag-unit! scv {:role :early-scout})
    (pop-build-order!)))

(defn- retry-build
  ([builder tag] (retry-build builder tag 0))
  ([builder tag jitter-amount]
     (macro-tag-unit! builder (merge tag {:retry (inc (:retry tag))}))
     (let [tx (+ (first (:args tag)) (* (Math/pow -1 (rand-int 2)) jitter-amount))
           ty (+ (second (:args tag)) (* (Math/pow -1 (rand-int 2)) jitter-amount))]
     (build builder tx ty (last (:args tag))))))

(defn- restart-failed-builders
  "SCVs that are idle but should be building probably ran into a
  problem while trying to build. Restart them."
  []
  (doseq [idle-scv (filter (every-pred completed? idle?) (my-scvs))]
    (let [tag (get-macro-tag idle-scv)]
      (when (= :build (:role tag))
        (println (str "Restarting failed builder " (get-id idle-scv)))
        (if (not (:jitter tag))
          (retry-build idle-scv tag)
          (retry-build idle-scv tag (mod (:retry tag) 20)))))))

(defn- mine-with-idle-scvs []
  (doseq [idle-scv (filter (every-pred completed? idle?) (my-scvs))]
    (cancel-contracts idle-scv)
    (macro-tag-unit! idle-scv {:available true})
    (micro-tag-unit! idle-scv {:role :mineral})))

(defn- maybe-train-scvs
  "Train SCVs if not already at maximum for number of expansions.
  NOTE: Doesn't actually do this yet. :)"
  []
  (doseq [cc (filter #(zero? (training-queue-size %)) (my-command-centers))]
    (when (can-afford? :scv)
      (contract-train cc :scv))))

(defn- maybe-train-army
  "Train army units from finished structures based on the desired unit
  composition."
  []
  (doseq [barracks (filter can-build-now? (my-barracks))]
    (when (can-afford? :marine)
      (contract-train barracks :marine))))

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
  (when (and (>= (+ (my-supply-used) 8) (contracted-max-supply))
             (can-afford? :supply-depot))
    (build-kw :supply-depot)))

(defn- assign-scvs-to-refineries
  "Ensure each functional refinery has three gas SCVs."
  []
  (let [assigned-to-refinery
        (fn [refinery scv]
          (and (completed? scv) (= refinery (:assigned (get-micro-tag scv)))))]
  (doseq [refinery (filter completed? (my-refineries))]
    (let [num-assigned (count (filter (partial assigned-to-refinery refinery) (my-scvs)))
          num-to-assign (max 0 (- 3 num-assigned))]
      (dotimes [n num-to-assign]
        (let [scv (assign-spare-scv! nil)]
          (micro-tag-unit! scv {:role :gas :assigned refinery})))))))

(defn run-macro-engine
  "Issue commands based on the current state of the game and the macro
  engine. Should be called in each gameUpdate loop."
  []
  (restart-failed-builders)
  (mine-with-idle-scvs)
  (assign-scvs-to-refineries)
  (maybe-train-scvs)
  (if (seq (:build-order @macro-state))
    (process-build-order-step)
    (do (ensure-enough-depots)
        (maybe-train-army))))
