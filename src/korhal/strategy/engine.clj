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

(defn run-strategy-engine []
  (let [enemy-base (first (enemy-start-locations))
        combat-units (filter (every-pred completed? combat-unit?) (my-units))]
    nil))

(defn start-strategy-engine! []
  (dosync
   (commute strategy-state assoc-in [:frame] 0) ;; BW does NOT reset the frame count when you restart a mission
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
                        (println "Strategy engine crash!")
                        (.printStackTrace e)
                        (dosync
                         (commute strategy-state assoc-in [:run] false))))
                      (dosync
                       (commute strategy-state assoc-in [:frame] frame)))
                  (Thread/sleep 1))
                (recur))))))

(defn stop-strategy-engine! []
  (dosync
   (commute strategy-state assoc-in [:run] false)))
