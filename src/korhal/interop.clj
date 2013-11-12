(ns korhal.interop
  (:refer-clojure :exclude [load])
  (:require [korhal.interop-types :refer [unit-types upgrade-types tech-types
                                          unit-type-fn-maps unit-fn-maps
                                          base-location-fn-maps player-fn-maps]])
  (:import (jnibwapi.model Map Player Unit BaseLocation Region ChokePoint)
           (jnibwapi.types.UnitType$UnitTypes)
           (jnibwapi.types.UpgradeType$UpgradeTypes)
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

(defn gen-unit-type-ids []
  (intern *ns*
          (symbol 'unit-type-ids)
          (->> (map #(vector (eval `(.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                             (eval (symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                   (take-nth 2 (rest unit-types)))
               (flatten)
               (apply hash-map))))
(gen-unit-type-ids)

(defn gen-unit-type-kw-lookup []
  (intern *ns*
          (symbol 'unit-type-kw-lookup)
          (merge {:mineral jnibwapi.types.UnitType$UnitTypes/Resource_Mineral_Field
                  :geyser jnibwapi.types.UnitType$UnitTypes/Resource_Vespene_Geyser}
                 (zipmap (map keyword (take-nth 2 unit-types))
                         (map #(eval `(. jnibwapi.types.UnitType$UnitTypes ~%)) (take-nth 2 (rest unit-types)))))))
(gen-unit-type-kw-lookup)

(defn gen-upgrade-type-ids []
  (intern *ns*
          (symbol 'upgrade-type-ids)
          (->> (map #(vector (eval `(.getID ~(symbol (str "jnibwapi.types.UpgradeType$UpgradeTypes/" %))))
                             (eval (symbol (str "jnibwapi.types.UpgradeType$UpgradeTypes/" %))))
                   (take-nth 2 (rest upgrade-types)))
               (flatten)
               (apply hash-map))))
(gen-upgrade-type-ids)

(defn gen-upgrade-type-kw-lookup []
  (intern *ns*
          (symbol 'upgrade-type-kw-lookup)
          (zipmap (map keyword (take-nth 2 upgrade-types))
                  (map #(eval `(. jnibwapi.types.UpgradeType$UpgradeTypes ~%)) (take-nth 2 (rest upgrade-types))))))
(gen-upgrade-type-kw-lookup)

(defn gen-tech-type-ids []
  (intern *ns*
          (symbol 'tech-type-ids)
          (->> (map #(vector (eval `(.getID ~(symbol (str "jnibwapi.types.TechType$TechTypes/" %))))
                             (eval (symbol (str "jnibwapi.types.TechType$TechTypes/" %))))
                   (take-nth 2 (rest tech-types)))
               (flatten)
               (apply hash-map))))
(gen-tech-type-ids)

(defn gen-tech-type-kw-lookup []
  (intern *ns*
          (symbol 'tech-type-kw-lookup)
          (zipmap (map keyword (take-nth 2 tech-types))
                  (map #(eval `(. jnibwapi.types.TechType$TechTypes ~%)) (take-nth 2 (rest tech-types))))))
(gen-tech-type-kw-lookup)

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
                                            (.getID (to-build unit-type-kw-lookup)))))

(defn build-addon [building to-build]
  (.buildAddon api (.getID building) (.getID (to-build unit-type-kw-lookup))))

(defn train [building to-train]
  (.train api (.getID building) (.getID (to-train unit-type-kw-lookup))))

(defn morph [unit morph-to]
  (.morph api (.getID unit) (.getID (morph-to unit-type-kw-lookup))))

(defn research [unit to-research]
  (.research api (.getID unit) (.getID (to-research tech-type-kw-lookup))))

(defn upgrade [unit to-upgrade]
  (.upgrade api (.getID unit) (.getID (to-upgrade upgrade-type-kw-lookup))))

(defn set-rally-point
  ([rally-unit target-unit-or-point]
     (cond
      (instance? java.awt.Point target-unit-or-point) (set-rally-point rally-unit
                                                                       (.x target-unit-or-point)
                                                                       (.y target-unit-or-point))
      :else (.setRallyPoint api (.getID rally-unit) (.getID target-unit-or-point))))
  ([rally-unit px py] (.setRallyPoint api (.getID rally-unit) px py)))

(defn move
  ([move-unit target-unit-or-point]
     (cond
      (instance? java.awt.Point target-unit-or-point) (move move-unit
                                                            (.x target-unit-or-point)
                                                            (.y target-unit-or-point))
      :else (.move api (.getID move-unit) (.getX target-unit-or-point) (.getY target-unit-or-point))))
  ([move-unit px py] (.move api (.getID move-unit) px py)))

(defn patrol
  ([patrol-unit target-unit-or-point]
     (cond
      (instance? java.awt.Point target-unit-or-point) (patrol patrol-unit
                                                              (.x target-unit-or-point)
                                                              (.y target-unit-or-point))
      :else (.patrol api (.getID patrol-unit) (.getX target-unit-or-point) (.getY target-unit-or-point))))
  ([patrol-unit px py] (.patrol api (.getID patrol-unit) px py)))

(defn hold-position [unit] (.holdPosition api (.getID unit)))

(defn stop [unit] (.stop api (.getID unit)))

(defn follow [follow-unit target-unit] (.follow api (.getID follow-unit) (.getID target-unit)))

(defn gather [gather-unit target-unit] (.gather api (.getID gather-unit) (.getID target-unit)))

(defn return-cargo [unit] (.returnCargo api unit))

(defn repair [repair-unit target-unit] (.repair api (.getID repair-unit) (.getID target-unit)))

(defn burrow [unit] (.burrow api (.getID unit)))

(defn unburrow [unit] (.unburrow api (.getID unit)))

(defn cloak [unit] (.cloak api (.getID unit)))

(defn decloak [unit] (.decloak api (.getID unit)))

(defn siege [unit] (.siege api (.getID unit)))

(defn unsiege [unit] (.unsiege api (.getID unit)))

(defn lift [unit] (.lift api (.getID unit)))

(defn land
  ([unit point] (land unit (.x point) (.y point)))
  ([unit tx ty] (.land api (.getID unit) tx ty)))

(defn load [loading-unit target-unit] (.load api (.getID loading-unit) (.getID target-unit)))

(defn unload [unloading-unit target-unit] (.unload api (.getID unloading-unit) (.getID target-unit)))

(defn unload-all
  ([unit] (.unloadAll api (.getID unit)))
  ([unit point] (unload-all unit (.x point) (.y point)))
  ([unit tx ty] (.unloadAll api (.getID unit) tx ty)))

(defn right-click
  ([unit target-unit-or-point]
     (cond
      (instance? java.awt.Point target-unit-or-point) (right-click unit
                                                                   (.x target-unit-or-point)
                                                                   (.y target-unit-or-point))
      :else (.rightClick api (.getID unit) (.getID target-unit-or-point))))
  ([unit px py] (.rightClick api (.getID unit) px py)))

(defn halt-construction [unit] (.haltConstruction api (.getID unit)))

(defn cancel-construction [unit] (.cancelConstrution api (.getID unit)))

(defn cancel-addon [unit] (.cancelAddon api (.getID unit)))

(defn cancel-train
  ([unit] (.cancelTrain api (.getID unit))) ;; cancels last slot being used
  ([unit slot] (.cancelTrain api (.getID unit) slot)))

(defn cancel-morph [unit] (.cancelMorph api (.getID unit)))

(defn cancel-research [unit] (.cancelResearch api (.getID unit)))

(defn cancel-upgrade [unit] (.cancelUpgrade api (.getID unit)))

(defn use-tech
  ([unit tech] (.useTech api (.getID unit) (.getID tech)))
  ([unit tech target-unit-or-point]
     (cond
      (instance? java.awt.Point target-unit-or-point) (use-tech unit
                                                                tech
                                                                (.x target-unit-or-point)
                                                                (.y target-unit-or-point))
      :else (.useTech api (.getID unit) (.getID tech) (.getID target-unit-or-point))))
  ([unit tech px py] (.useTech api (.getID unit) (.getID tech) px py)))

(defn place-cop
  ([unit point] (place-cop unit (.x point) (.y point)))
  ([unit tx ty] (.placeCOP api (.getID unit) tx ty)))

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
