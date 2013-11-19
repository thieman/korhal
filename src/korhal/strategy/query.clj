(ns korhal.strategy.query
  (:require [korhal.interop.interop :refer :all]))

(def strategy-state (ref {:enemy-units {} :nukes {} :frame 0}))

(defn get-priority-enemy-base []
  (first (enemy-start-locations)))
