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

(defn micro-tag-new-unit! [unit]
  (let [unit-type (get-unit-type unit)]
    (condp = unit-type
      (get-unit-type (:scv unit-type-kws)) nil
      (micro-tag-unit! unit {:role :defend}))))

(defn- micro-mineral-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-gas? unit)))
    (when-let [closest-mineral (closest unit (minerals))]
      (with-api (right-click unit closest-mineral)))))

(defn- micro-gas-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-minerals? unit)))
    (when-let [closest-refinery (closest unit (my-refineries))]
      (when (and closest-refinery (completed? closest-refinery))
        (with-api (right-click unit closest-refinery))))))

(defn- micro-early-scout [unit]
  (let [enemy-base (first (enemy-start-locations))]
    (with-api (move unit (pixel-x enemy-base) (pixel-y enemy-base)))))

(defn- micro-defender [unit base-choke]
  (when (and (completed? unit)
             (idle? unit))
    (if (> (dist-choke unit base-choke) 300)
      (with-api (attack unit (center-x base-choke) (center-y base-choke)))
      (when (and (is-siege-tank-tank-mode? unit) (researched? :tank-siege-mode))
        (siege unit)))))

(defn- micro-attacker [unit attack-location]
  (when ((every-pred completed? idle?) unit)
    (with-api (attack unit (pixel-x attack-location) (pixel-y attack-location)))))

(defn dispatch-on-role [role unit] role)
(defmulti micro-combat-role dispatch-on-role)

(defmethod micro-combat-role :worker [role unit])

(defmethod micro-combat-role :attack [role unit])

(defmethod micro-combat-role :stim [role unit]
  (if (and (or (is-marine? unit) (is-firebat? unit))
           (>= (health-perc unit) 0.5)
           (researched? :stim-packs))
    (with-api
      (when-not (stimmed? unit)
        (use-tech unit (tech-type-kws :stim-packs))))))

(defmethod micro-combat-role :kite [role unit]
  (let [close-melee?
        (fn [enemy]
          (and (ground-melee? enemy)
               (<= (dist unit enemy) (max-range (ground-weapon unit)))))
        enemy-melee (filter close-melee? (enemy-units))]
    (when (seq enemy-melee)
      (let [closest (apply min-key (partial dist unit) enemy-melee)]
        (with-api
          (move-angle unit (angle-away unit closest) 50))))))

(defmethod micro-combat-role :default [role unit])

(defn micro-combat [unit]
  (doseq [role (strat/combat-roles (get-unit-type-kw unit))]
    (micro-combat-role role unit)))

(defn micro-under-aoe [unit storms]
  (let [storm-range 288]
    (when (and (under-storm? unit) (not (moving? unit)))
      (with-api (move unit (- (pixel-x unit) 100) (- (pixel-y unit) 100))))))

(defn run-micro-engine []
  (let [base-choke (closest-choke-start (my-start-location) (chokepoints))
        enemy-base (strat/get-priority-enemy-base)
        storms (filter #(= (bullet-type-kws :psionic-storm) (get-type-id %)) (bullets))]
    (doseq [unit (filter (complement building?) (my-units))]
      (cond
       (under-aoe? unit) (micro-under-aoe unit storms)
       (or (attacking? unit) (under-attack? unit)) (micro-combat unit)
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
