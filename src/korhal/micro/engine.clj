(ns korhal.micro.engine
  (:require [korhal.interop.interop :refer :all]
            [korhal.micro.role :refer [micro-mineral-worker micro-gas-worker
                                       micro-early-scout micro-defender
                                       micro-attacker micro-under-aoe]]
            [korhal.micro.combat :refer [micro-combat]]
            [korhal.strategy.query :as strat]
            [korhal.tools.repl :refer [repl-control]]
            [korhal.tools.queue :refer [with-api with-api-when]])
  (:import (jnibwapi.model Unit)))

(def micro-state (ref {:tags {} :frame 0}))

(defn micro-tag-unit! [unit-or-unit-id tag]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (dosync
     (commute micro-state assoc-in [:tags unit-id] tag))))

(defn get-micro-tag [unit-or-unit-id]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (get-in @micro-state [:tags unit-id])))

(defn micro-tag-new-unit! [unit]
  (let [unit-type (get-unit-type unit)]
    (condp = unit-type
      (get-unit-type (:scv unit-type-kws)) nil
      (micro-tag-unit! unit {:role :defend}))))

(defn run-micro-engine []
  (let [base-choke (closest-choke-start (my-start-location) (chokepoints))
        enemy-base (strat/get-priority-enemy-base)
        storms (filter #(= (bullet-type-kws :psionic-storm) (get-type-id %)) (bullets))]
    (doseq [unit (filter (complement building?) (my-units))]
      (cond
       (under-aoe? unit) (micro-under-aoe unit storms)
       (or (attacking? unit)
           (under-attack? unit)
           (units-nearby unit 1000 (enemy-units))) (micro-combat unit)
       :else (condp = (:role (get-micro-tag unit))
               nil nil
               :mineral (micro-mineral-worker unit)
               :gas (micro-gas-worker unit)
               :early-scout (micro-early-scout unit)
               :defend (micro-defender unit base-choke)
               :attack (micro-attacker unit enemy-base)
               :else nil)))))

(defn start-micro-engine! []
  (dosync
   (commute micro-state assoc-in [:tags] {})
   (commute micro-state assoc-in [:frame] 0)
   (commute micro-state assoc-in [:run] true))
  (future (loop []
            (if (not (:run @micro-state))
              nil
              (let [frame (frame-count)]
                (if (and (> frame (:frame @micro-state))
                         (not @repl-control))
                  (do (try
                        (run-micro-engine)
                      (catch Exception e
                        (println "Micro engine exception!")
                        (.printStackTrace e)))
                      (dosync
                       (commute micro-state assoc-in [:frame] frame)))
                  (Thread/sleep 1))
                (recur))))))

(defn stop-micro-engine! []
  (dosync
   (commute micro-state assoc-in [:run] false)))
