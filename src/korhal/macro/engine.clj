(ns korhal.macro.engine
  (:require [korhal.interop.interop :refer :all]
            [korhal.macro.state :refer [macro-state macro-tag-unit! get-macro-tag pop-build-order!]]
            [korhal.macro.command :refer :all]
            [korhal.macro.build-order :refer [build-orders get-random-build-order]]
            [korhal.micro.state :refer [micro-tag-unit! get-micro-tag]]
            [korhal.tools.queue :refer [with-api]]
            [korhal.tools.repl :refer [repl-control]]
            [korhal.tools.contract :refer [cancel-contracts clear-contracts
                                           contract-train
                                           can-afford? contracted-max-supply
                                           contracted-addons can-make-now?]]
            [korhal.tools.util :refer [profile]]))

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
       (with-api (build builder tx ty (last (:args tag)))))))

(defn- retry-failed-addons
  "Addons that could not be built should be retried."
  []
  (let [idle-building? (fn [b] (and (completed? b) (zero? (training-queue-size b))))]
    (doseq [building (filter idle-building? (my-buildings))]
      (when-let [addon (first (contracted-addons building))]
        (with-api (build-addon building (:kw addon)))))))

(defn- restart-failed-building-scvs
  "SCVs that are idle or gathering but should be building probably ran
  into a problem while trying to build. Restart them."
  []
  (doseq [idle-scv (filter #(and (completed? %)
                                 (or (idle? %) (gathering-minerals? %) (gathering-gas? %)))
                           (my-scvs))]
    (let [tag (get-macro-tag idle-scv)]
      (when (= :build (:role tag))
        (micro-tag-unit! idle-scv nil)
        (if (not (:jitter tag))
          (retry-build idle-scv tag)
          (retry-build idle-scv tag (Math/floor (/ (:retry tag) 20))))))))

(defn- mine-with-idle-scvs []
  (doseq [idle-scv (filter (every-pred completed? idle?) (my-scvs))]
    (when-not (get-macro-tag idle-scv)
      (cancel-contracts idle-scv)
      (macro-tag-unit! idle-scv {:available true})
      (micro-tag-unit! idle-scv {:role :mineral}))))

(defn- mine-with-unassigned-gas-scvs
  "We have to do this because 1. SCVs automatically start mining gas
  once they finish a refinery, 2. the unitComplete callback is bugged
  and does not get fired when a refinery finishes, and 3. interfacing
  with SCVs currently inside of a refinery seems pretty wonky in
  general. We put any unassigned SCVs that happen to be gathering gas
  into a mineral line, then separately assign gas SCVs in a different
  function."
  []
  (doseq [gas-scv (filter gathering-gas? (my-scvs))]
    (when (not= :gas (:role (get-micro-tag gas-scv)))
      (macro-tag-unit! gas-scv {:available true})
      (micro-tag-unit! gas-scv {:role :mineral}))))

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
    (cond
     (can-make-now? barracks :medic) (contract-train barracks :medic)
     (can-make-now? barracks :marine) (contract-train barracks :marine)))
  (doseq [factory (filter can-build-now? (my-factories))]
    (cond
     (can-make-now? factory :siege-tank-tank-mode) (contract-train factory :siege-tank-tank-mode)
     (can-make-now? factory :goliath) (contract-train factory :goliath)
     (can-make-now? factory :vulture) (contract-train factory :vulture))))

(defn process-build-order-step []
  (let [[directive kw] (first (partition 2 (:build-order @macro-state)))]
    (if (number? directive)
      (when (>= (my-supply-used) directive)
        (cond
         (= kw :wait) (pop-build-order!)
         (= kw :send-scout) (send-early-game-scout)
         :else (case kw
                 :command-center (expand true)
                 :refinery (build-refinery true)
                 (build-kw kw true))))
      (when (can-afford? kw)
        (case directive
          :addon (addon-kw kw)
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
    (let [assigned (filter (partial assigned-to-refinery refinery) (my-scvs))
          num-to-assign (max 0 (- 3 (count assigned)))]
      (if (> (count assigned) 3)
        (let [reassign (first (filter #(not= (order-id %) (get-id (:harvest-gas order-type-kws))) assigned))]
          (micro-tag-unit! reassign nil)
          (macro-tag-unit! reassign nil)
          (with-api (stop reassign)))
        (dotimes [n num-to-assign]
          (let [scv (assign-spare-scv! nil)]
            (micro-tag-unit! scv {:role :gas :assigned refinery}))))))))

(defn run-macro-engine
  "Issue commands based on the current state of the game and the macro
  engine. Should be called in each gameUpdate loop."
  []
  (clear-contracts)
  (retry-failed-addons)
  (restart-failed-building-scvs)
  (mine-with-idle-scvs)
  (mine-with-unassigned-gas-scvs)
  (assign-scvs-to-refineries)
  (maybe-train-scvs)
  (if (seq (:build-order @macro-state))
    (process-build-order-step)
    (do (ensure-enough-depots)
        (maybe-train-army))))

(defn start-macro-engine! []
  (dosync
   (commute macro-state assoc-in [:build-order] (build-orders :test-order))
   (commute macro-state assoc-in [:tags] {})
   (commute macro-state assoc-in [:frame] 0)
   (commute macro-state assoc-in [:run] true))
  (future (loop []
            (if (not (:run @macro-state))
              nil
              (let [frame (frame-count)]
                (if (and (> frame (:frame @macro-state)) (not @repl-control))
                  (do (try
                        (run-macro-engine)
                      (catch Exception e
                        (println "Macro engine exception!")
                        (.printStackTrace e)))
                      (dosync
                       (commute macro-state assoc-in [:frame] frame)))
                  (Thread/sleep 1))
                (recur))))))

(defn stop-macro-engine! []
  (dosync
   (commute macro-state assoc-in [:run] false)))
