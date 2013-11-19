(ns korhal.micro.engine
  (:require [korhal.interop.interop :refer :all]
            [korhal.strategy.query :as strat]
            [korhal.tools.repl :refer [repl-control]]
            [korhal.tools.queue :refer [with-api]])
  (:import (jnibwapi.model Unit)))

(def micro-state (ref {:tags {} :frame 0}))

(defn micro-tag-unit! [unit-or-unit-id tag]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (dosync
     (commute micro-state assoc-in [:tags unit-id] tag))))

(defn get-micro-tag [unit-or-unit-id]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (get-in @micro-state [:tags unit-id])))

(defn- micro-mineral-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-gas? unit)))
    (let [closest-mineral (apply min-key (partial dist unit) (minerals))]
      (with-api (right-click unit closest-mineral)))))

(defn- micro-gas-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-minerals? unit)))
    (let [closest-refinery (apply min-key (partial dist unit) (my-refineries))]
      (when (and closest-refinery (completed? closest-refinery))
        (with-api (right-click unit closest-refinery))))))

(defn- micro-early-scout [unit]
  (let [enemy-base (first (enemy-start-locations))]
    (with-api (move unit (pixel-x enemy-base) (pixel-y enemy-base)))))

(defn- micro-defender [unit base-choke]
  (when (and (completed? unit)
             (idle? unit)
             (> (dist-choke unit base-choke) 200))
    (with-api (right-click unit (center-x base-choke) (center-y base-choke)))))

(defn- micro-attacker [unit attack-location]
  (when ((every-pred completed? idle?) unit)
    (with-api (attack unit (pixel-x attack-location) (pixel-y attack-location)))))

(defn micro-tag-new-unit! [unit]
  (let [unit-type (get-unit-type unit)]
    (condp = unit-type
      (get-unit-type (:scv unit-type-kws)) nil
      (micro-tag-unit! unit {:role :defend}))))

(defn run-micro-engine []
  (let [base-choke (apply min-key (partial dist-choke (first (my-command-centers))) (chokepoints))
        enemy-base (strat/get-priority-enemy-base)]
    (doseq [unit (filter (complement building?) (my-units))]
      (condp = (:role (get-micro-tag unit))
        nil nil
        :mineral (micro-mineral-worker unit)
        :gas (micro-gas-worker unit)
        :early-scout (micro-early-scout unit)
        :defend (micro-defender unit base-choke)
        :attack (micro-attacker unit enemy-base)
        :else nil))))

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
                        (println "Micro engine crash!")
                        (.printStackTrace e)
                        (dosync
                         (commute micro-state assoc-in [:run] false))))
                      (dosync
                       (commute micro-state assoc-in [:frame] frame)))
                  (Thread/sleep 1))
                (recur))))))

(defn stop-micro-engine! []
  (dosync
   (commute micro-state assoc-in [:run] false)))
