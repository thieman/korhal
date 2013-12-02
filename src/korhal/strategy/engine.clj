(ns korhal.strategy.engine
  (:require [korhal.interop.interop :refer :all]
            [korhal.strategy.query :refer [strategy-state]]
            [korhal.micro.engine :refer [micro-tag-unit!]]
            [korhal.tools.repl :refer [repl-control]]
            [korhal.tools.queue :refer [with-api]]))

(defn strategy-inform! [tag-type doc]
  (dosync
   (if (:id doc)
     (commute strategy-state assoc-in [tag-type (:id doc)] doc)
     (commute strategy-state update-in [tag-type] conj doc))))

(defn strategy-remove! [tag-type id]
  (dosync
   (commute strategy-state update-in [tag-type] #(dissoc % id))))

(defn strategy-expire! [tag-type frame-diff]
  (let [expired? (fn [doc] (>= (- (frame-count) (:frame doc)) frame-diff))]
    (dosync
     (commute strategy-state update-in [tag-type] #(remove expired? %)))))

(defn draw-squads-display []
  (let [squads (:squad-members @strategy-state)]
    (doseq [unit (my-units)]
      (when-let [squad (squads unit)]
        (draw-text (pixel-x unit) (pixel-y unit) squad false)))))

(defn reform-squads []
  (loop [latest-id 0
         squads {}
         rest' (remove worker? (my-units))]
    (if-not (seq rest')
      (dosync
       (commute strategy-state assoc :squad-members squads))
      (let [unit (first rest')
            closest-ally (closest unit (keys squads))
            ally-squad (squads closest-ally)]
        (if (and ally-squad (<= (dist unit closest-ally) 300))
          (recur latest-id (assoc squads unit ally-squad) (next rest'))
          (recur (inc latest-id) (assoc squads unit latest-id) (next rest')))))))

(defn run-strategy-engine []
  (let [enemy-base (first (enemy-start-locations))
        combat-units (filter (every-pred completed? combat-unit?) (my-units))]
    (reform-squads)))

(defn start-strategy-engine! []
  (dosync
   (commute strategy-state assoc-in [:frame] 0)
   (commute strategy-state assoc-in [:run] true))
  (future (loop []
            (if (not (:run @strategy-state))
              nil
              (let [frame (frame-count)]
                (if (and (> frame (:frame @strategy-state))
                         (not @repl-control))
                  (do (try
                        (run-strategy-engine)
                      (catch Exception e
                        (println "Strategy engine exception!")
                        (.printStackTrace e)))
                      (dosync
                       (commute strategy-state assoc-in [:frame] frame)))
                  (Thread/sleep 1))
                (recur))))))

(defn stop-strategy-engine! []
  (dosync
   (commute strategy-state assoc-in [:run] false)))
