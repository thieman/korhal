(ns korhal.strategy.query
  (:require [korhal.interop.interop :refer :all]))

(def strategy-state (ref {:enemy-units {} :nukes {} :frame 0}))

(def combat-roles
  {:scv [:worker]
   :marine [:stim :kite :attack]
   :firebat [:stim :attack]
   :medic [:cower :heal]
   :ghost [:cloak :lockdown :attack]
   :vulture [:kite :attack]
   :goliath [:attack]
   :siege-tank-tank-mode [:attack]
   :siege-tank-siege-mode [:attack]
   :dropship [:transport]
   :wraith [:cloak :attack]
   :science-vessel [:science-vessel]
   :battlecruiser [:yamato :attack]})

(defn get-priority-enemy-base []
  (first (enemy-start-locations)))
