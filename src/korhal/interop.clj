(ns korhal.interop
  (:import (jnibwapi.types.UnitType$UnitTypes)))

(def ^:dynamic api nil)
(defn bind-api [binding] (alter-var-root (var api) #(identity %2) binding))

;; type lookups

(def type-lookup
  {:drone 'Zerg_Drone
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
  (println (.getID builder))
  (println (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(to-build type-lookup)))))
  (println tile-x)
  (println tile-y)
  (.build api (.getID builder) tile-x tile-y
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(to-build type-lookup))))))

(defn morph [unit morph-to]
  (.morph api
          (.getID unit)
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(morph-to type-lookup))))))

;; TODO: clean up these macros and reduce duplication through higher-order fn

(defn def-type-predicate [clj-name java-name]
  `(defn ~(symbol (str "is-" clj-name "?")) [unit#]
     (= (.getTypeID unit#) (.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" java-name))))))

(defn def-my-unit-group [clj-name java-name]
  `(defn ~(symbol (str "my-" clj-name "s")) []
     (filter ~(symbol (str "is-" clj-name "?")) (.getMyUnits api))))

(defmacro def-type-predicates [& forms]
  (cons `do
        (for [[clj-name java-name] (partition 2 forms)]
          (def-type-predicate clj-name java-name))))

(defmacro def-my-unit-groups [& forms]
  (cons `do
        (for [[clj-name java-name] (partition 2 forms)]
          (def-my-unit-group clj-name java-name))))

(def-type-predicates
  mineral Resource_Mineral_Field
  larva Zerg_Larva
  drone Zerg_Drone
  overlord Zerg_Overlord
  zergling Zerg_Zergling)

(def-my-unit-groups
  drone Zerg_Drone
  larva Zerg_Larva
  overlord Zerg_Overlord
  zergling Zerg_Zergling)
