(ns korhal.strategy.query
  (:require [korhal.interop.interop :refer :all]))

(def strategy-state (ref {:frame 0}))

(defn get-priority-enemy-base []
  (first (enemy-start-locations)))
