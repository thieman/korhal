(ns korhal.strategy.engine
  (:require [korhal.interop.interop :refer :all]
            [korhal.strategy.query :refer [strategy-state]]
            [korhal.micro.engine :refer [micro-tag-unit!]]
            [korhal.tools.repl :refer [repl-control]]
            [korhal.tools.queue :refer [with-api]])
  (:import (jnibwapi.model Unit)))

(defn run-strategy-engine []
  (let [enemy-base (first (enemy-start-locations))
        combat-units (filter (every-pred completed? combat-unit?) (my-units))]
    (when (>= (count combat-units) 12)
      (doseq [unit combat-units]
        (micro-tag-unit! unit {:role :attack})))))

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
