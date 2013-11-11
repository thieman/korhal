(ns korhal.interop
  (:import (jnibwapi.model.Unit)
           (jnibwapi.types.UnitType$UnitTypes)))

(def ^:dynamic api nil)
(defn bind-api [binding] (alter-var-root (var api) #(identity %2) binding))

;; type definitions

(def unit-types
  ['larva 'Zerg_Larva
   'drone 'Zerg_Drone
   'overlord 'Zerg_Overlord
   'zergling 'Zerg_Zergling])

(def type-lookup
  {:mineral 'Resource_Mineral_Field
   :geyser 'Resource_Vespene_Geyser
   :drone 'Zerg_Drone
   :larva 'Zerg_Larva
   :overlord 'Zerg_Overlord
   :zergling 'Zerg_Zergling
   :spawning-pool 'Zerg_Spawning_Pool})

;; common calls to get state vars and collections

(defn my-minerals [] (.. api getSelf getMinerals))

(defn my-gas [] (.. api getSelf getGas))

(defn my-supply-used [] (.. api getSelf getSupplyUsed))

(defn my-supply-total [] (.. api getSelf getSupplyTotal))

(defn my-units [] (.getMyUnits api))

(defn enemy-units [] (.getEnemyUnits api))

(defn neutral-units [] (.getNeutralUnits api))

(defn minerals []
  (filter #(= (.getTypeID %) (.getID jnibwapi.types.UnitType$UnitTypes/Resource_Mineral_Field))
          (.getNeutralUnits api)))

(defn geysers []
  (filter #(= (.getTypeID %) (.getID jnibwapi.types.UnitType$UnitTypes/Resource_Vespene_Geyser))
          (.getNeutralUnits api)))

;; targeted predicates

(defn is-idle? [obj] (.isIdle obj))

;; common API commands

(defn get-id [obj] (.getID obj))

(defn right-click [selected target]
  (.rightClick api (.getID selected) (.getID target)))

(defn get-tile-x [obj] (.getTileX obj))

(defn get-tile-y [obj] (.getTileY obj))

;; unit commands

(defn attack [unit target]
  (.attack api (.getID unit) (.getX target) (.getY target)))

(defn build [builder tile-x tile-y to-build]
  (.build api (.getID builder) tile-x tile-y
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(to-build type-lookup))))))

(defn morph [unit morph-to]
  (.morph api
          (.getID unit)
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(morph-to type-lookup))))))

;; type predicates, e.g. is-drone?
(doseq [[n t] (partition 2 unit-types)]
  (let [class-type (eval `(.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" t))))]
    (intern *ns*
            (symbol (str "is-" n "?"))
            (fn [unit] (= (.getTypeID unit) class-type)))))

;; own unit type collections, e.g. my-drones
(doseq [[n _] (partition 2 unit-types)]
  (let [type-predicate (eval (symbol (str *ns* "/is-" n "?")))]
    (intern *ns*
            (symbol (str "my-" n "s"))
            (fn [] (filter type-predicate (.getMyUnits api))))))
