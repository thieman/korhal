(ns korhal.interop
  (:require [korhal.interop-types :refer [unit-types unit-type-fn-maps unit-fn-maps
                                          base-location-fn-maps player-fn-maps]])
  (:import (jnibwapi.model Map Player Unit BaseLocation Region ChokePoint)
           (jnibwapi.types.UnitType$UnitTypes)
           (java.awt.Point)))

(declare get-type pixel-x pixel-y tile-x tile-y start-location?)

(def api nil)
(defn bind-api! [binding] (alter-var-root #'api #(identity %2) binding))

(defn dynamic-dot-form [instance method] `(. ~instance ~method))

;; type conversions

(defn java-point [obj grid-type]
  (condp = grid-type
    :pixel (java.awt.Point. (pixel-x obj) (pixel-y obj))
    :tile (java.awt.Point. (tile-x obj) (tile-y obj))))

;; type definitions

(defn gen-type-ids []
  (intern *ns*
          (symbol 'type-ids)
          (->> (map #(vector (eval `(.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                             (eval (symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                   (take-nth 2 (rest unit-types)))
               (flatten)
               (apply hash-map))))
(gen-type-ids)

(defn gen-type-kw-lookup []
  (intern *ns*
          (symbol 'type-kw-lookup)
          (merge {:mineral jnibwapi.types.UnitType$UnitTypes/Resource_Mineral_Field
                  :geyser jnibwapi.types.UnitType$UnitTypes/Resource_Vespene_Geyser}
                 (zipmap (map keyword (take-nth 2 unit-types))
                         (map #(eval `(. jnibwapi.types.UnitType$UnitTypes ~%)) (take-nth 2 (rest unit-types)))))))

(gen-type-kw-lookup)

;; common calls to get state vars and collections

(defn get-self [] (. api getSelf))

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

;; map data

(defn map-width [] (.. api getMap getMapWidth))

(defn map-height [] (.. api getMap getMapHeight))

(defn map-name [] (.. api getMap getMapName))

(defn region-map [] (.. api getMap getRegionMap))

(defn walkable-data [] (.. api getMap getWalkableData))

(defn buildable-data [] (.. api getMap getBuildableData))

(defn chokepoints [] (.. api getMap getChokePoints))

(defn regions [] (.. api getMap getRegions))

(defn polygon [region-id] (.. api getMap getPolygon region-id))

(defn base-locations [] (.. api getMap getBaseLocations))

(defn my-start-location [] (.. api getSelf getStartLocation))

(defn enemy-start-locations []
  (let [bases (base-locations)
        enemy-base? (fn [base] (and (not (= (java-point base :tile) (my-start-location)))
                                    (start-location? base)))]
    (filter enemy-base? bases)))

;; generate player methods

(defmacro define-player-fns []
  (cons `do
        (for [[clj-name java-name] (partition 2 player-fn-maps)]
          `(defn ~clj-name [player#] (. player# ~java-name)))))

(define-player-fns)

(defn has-researched [player tech] (.hasResearched player (.getID tech)))

(defn is-researching [player tech] (.isResearching player (.getID tech)))

(defn upgrade-level [player upgrade] (.upgradeLevel player (.getID upgrade)))

(defn is-upgrading [player upgrade] (.isUpgrading player (.getID upgrade)))

;; generate base location methods

(defmacro define-base-location-fns []
  (cons `do
        (for [[clj-name java-name] (partition 2 base-location-fn-maps)]
          `(defn ~clj-name [loc#] (. loc# ~java-name)))))

(define-base-location-fns)

;; generate single unit functions

(defmacro define-unit-type-fns []
  (cons `do
        (for [[clj-name java-name] (partition 2 unit-type-fn-maps)]
          `(defn ~clj-name [unit#] (. (get-type unit#) ~java-name)))))

(define-unit-type-fns)

(defmacro define-unit-fns []
  (cons `do
        (for [[clj-name java-name] (partition 2 unit-fn-maps)]
          `(defn ~clj-name [unit#] (. unit# ~java-name)))))

(define-unit-fns)

;; common API commands shared among multiple types

(defn get-id [obj] (.getID obj))

(defn get-type [unit] (.getUnitType api (.getTypeID unit)))

(defn right-click [selected target]
  (.rightClick api (.getID selected) (.getID target)))

(defn pixel-x [obj] (.getX obj))

(defn pixel-y [obj] (.getY obj))

(defn tile-x [obj]
  (if (instance? BaseLocation obj)
    (.getTx obj)
    (.getTileX obj)))

(defn tile-y [obj]
  (if (instance? BaseLocation obj)
    (.getTy obj)
    (.getTileY obj)))

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

;; API unit commands

(defn attack
  ([attacking-unit target-unit] (.attack api (.getID attacking-unit) (.getID target-unit)))
  ([attacking-unit px py] (.attack api (.getID attacking-unit) px py)))

(defn build
  ([builder point to-build] (build builder (.x point) (.y point) to-build))
  ([builder tile-x tile-y to-build] (.build api (.getID builder) tile-x tile-y
                                            (.getID (to-build type-kw-lookup)))))

(defn build-addon [building to-build]
  (.buildAddon api (.getID building) (.getID (to-build type-kw-lookup))))

(defn train [building to-train]
  (.train api (.getID building) (.getID (to-train type-kw-lookup))))

(defn morph [unit morph-to]
  (.morph api (.getID unit) (.getID (morph-to type-kw-lookup))))

;; utility functions

(defn swap-key [curr-val k v]
  (merge curr-val {k v}))

(defn swap-keys [swap-atom & forms]
  (doseq [[k v] (partition 2 forms)]
    (swap! swap-atom swap-key k v)))

(defn dist [a b]
  (Math/sqrt (+ (Math/pow (- (pixel-x a) (pixel-x b)) 2) (Math/pow (- (pixel-y a) (pixel-y b)) 2))))

(defn dist-tile [a b]
  (Math/sqrt (+ (Math/pow (- (tile-x a) (tile-x b)) 2) (Math/pow (- (tile-y a) (tile-y b)) 2))))
