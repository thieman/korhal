(ns korhal.interop.interop
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop-types :refer [unit-types upgrade-types tech-types
                                                  unit-command-types race-types unit-size-types
                                                  weapon-types bullet-types damage-types
                                                  explosion-types order-types
                                                  unit-type-fn-maps unit-fn-maps
                                                  base-location-fn-maps player-fn-maps]]
            [korhal.tools.util :refer [swap-key swap-keys plural]])
  (:import (jnibwapi.model Map Player Unit BaseLocation Region ChokePoint)
           (jnibwapi.types.UnitType$UnitTypes)
           (jnibwapi.types.UpgradeType$UpgradeTypes)
           (jnibwapi.types.TechType$TechTypes)
           (jnibwapi.types.UnitCommandType$UnitCommandTypes)
           (jnibwapi.types.RaceType$RaceTypes)
           (jnibwapi.types.UnitSizeType$UnitSizeTypes)
           (jnibwapi.types.WeaponType$WeaponTypes)
           (jnibwapi.types.BulletType$BulletTypes)
           (jnibwapi.types.DamageType$DamageTypes)
           (jnibwapi.types.ExplosionType$ExplosionTypes)
           (jnibwapi.types.OrderType$OrderTypes)
           (java.awt.Point)))

(declare get-type pixel-x pixel-y tile-x tile-y start-location? can-build-here? get-type-id)

(def api nil)
(defn bind-api! [binding] (alter-var-root #'api #(identity %2) binding))

(defn dynamic-dot-form [instance method] `(. ~instance ~method))

;; type conversions

(defn java-point [obj grid-type]
  (condp = grid-type
    :pixel (java.awt.Point. (pixel-x obj) (pixel-y obj))
    :tile (java.awt.Point. (tile-x obj) (tile-y obj))))

;; type definitions

;; unit type kw lookup is a special case to add in the minerals and geysers
(def unit-type-kws
  (merge {:mineral jnibwapi.types.UnitType$UnitTypes/Resource_Mineral_Field
          :geyser jnibwapi.types.UnitType$UnitTypes/Resource_Vespene_Geyser}
         (zipmap (map keyword (take-nth 2 unit-types))
                 (map #(eval `(. jnibwapi.types.UnitType$UnitTypes ~%)) (take-nth 2 (rest unit-types))))))

(defmacro gen-type-ids-map [inject-sym java-type coll]
  `(def ~inject-sym
     (->> (map #(vector (eval `(.getID ~(symbol (str ~java-type "/" %))))
                        (eval (symbol (str ~java-type "/" %))))
               (take-nth 2 (rest ~coll)))
          (flatten)
          (apply hash-map))))

(defmacro gen-type-kw-map [inject-sym java-type coll]
  `(def ~inject-sym
     (zipmap (map keyword (take-nth 2 ~coll))
             (map #(eval `(. ~~java-type ~%)) (take-nth 2 (rest ~coll))))))

(gen-type-ids-map unit-type-ids 'jnibwapi.types.UnitType$UnitTypes unit-types)
(gen-type-ids-map upgrade-type-ids 'jnibwapi.types.UpgradeType$UpgradeTypes upgrade-types)
(gen-type-ids-map tech-type-ids 'jnibwapi.types.TechType$TechTypes tech-types)
(gen-type-ids-map unit-command-type-ids 'jnibwapi.types.UnitCommandType$UnitCommandTypes unit-command-types)
(gen-type-ids-map race-type-ids 'jnibwapi.types.RaceType$RaceTypes race-types)
(gen-type-ids-map unit-size-type-ids 'jnibwapi.types.UnitSizeType$UnitSizeTypes unit-size-types)
(gen-type-ids-map weapon-type-ids 'jnibwapi.types.WeaponType$WeaponTypes weapon-types)
(gen-type-ids-map bullet-type-ids 'jnibwapi.types.BulletType$BulletTypes bullet-types)
(gen-type-ids-map damage-type-ids 'jnibwapi.types.DamageType$DamageTypes damage-types)
(gen-type-ids-map explosion-type-ids 'jnibwapi.types.ExplosionType$ExplosionTypes explosion-types)
(gen-type-ids-map order-type-ids 'jnibwapi.types.OrderType$OrderTypes order-types)

(gen-type-kw-map upgrade-type-kws 'jnibwapi.types.UpgradeType$UpgradeTypes upgrade-types)
(gen-type-kw-map tech-type-kws 'jnibwapi.types.TechType$TechTypes tech-types)
(gen-type-kw-map unit-command-type-kws 'jnibwapi.types.UnitCommandType$UnitCommandTypes unit-command-types)
(gen-type-kw-map race-type-kws 'jnibwapi.types.RaceType$RaceTypes race-types)
(gen-type-kw-map unit-size-type-kws 'jnibwapi.types.UnitSizeType$UnitSizeTypes unit-size-types)
(gen-type-kw-map weapon-type-kws 'jnibwapi.types.WeaponType$WeaponTypes weapon-types)
(gen-type-kw-map bullet-type-kws 'jnibwapi.types.BulletType$BulletTypes bullet-types)
(gen-type-kw-map damage-type-kws 'jnibwapi.types.DamageType$DamageTypes damage-types)
(gen-type-kw-map explosion-type-kws 'jnibwapi.types.ExplosionType$ExplosionTypes explosion-types)
(gen-type-kw-map order-type-kws 'jnibwapi.types.OrderType$OrderTypes order-types)

;; common calls to get state vars and collections

(defn get-self [] (. api getSelf))

(defn my-minerals [] (.. api getSelf getMinerals))

(defn my-gas [] (.. api getSelf getGas))

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

(defn get-map [] (.getMap api))

(defn load-map-data [boolean] (.loadMapData api boolean))

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
          `(defn ~clj-name [unit-or-unit-type#]
               (. (get-type unit-or-unit-type#) ~java-name)))))

(define-unit-type-fns)

(defmacro define-unit-fns []
  (cons `do
        (for [[clj-name java-name] (partition 2 unit-fn-maps)]
          `(defn ~clj-name [unit#] (. unit# ~java-name)))))

(define-unit-fns)

;; supply functions are dumb and return double the supply to accommodate
;; 0.5 supply zerglings while still using an int type
;; Clojure isn't statically typed, so we don't have to put up with that shit

(defn my-supply-used [] (/ (.. api getSelf getSupplyUsed) 2))

(defn my-supply-total [] (/ (.. api getSelf getSupplyTotal) 2))

(defn supply-provided [unit]
  (/ (. (get-type unit) getSupplyProvided) 2))

(defn supply-required [unit]
  (/ (. (get-type unit) getSupplyRequired) 2))

;; common API commands shared among multiple types

(defn get-id [obj] (.getID obj))

(defn get-type [unit-or-unit-type]
  (if (instance? Unit unit-or-unit-type)
    (.getUnitType api (.getTypeID unit-or-unit-type))
    (.getUnitType api (.getID unit-or-unit-type))))

(defn get-type-id [unit-or-unit-type]
  (if (instance? Unit unit-or-unit-type)
    (.getTypeID unit-or-unit-type)
    (.getID unit-or-unit-type)))

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
(doseq [[n t] (partition 2 unit-types)]
  (when (not (re-seq #"^Critter" (str t)))
    (let [type-predicate (eval (symbol (str *ns* "/is-" n "?")))]
      (intern *ns*
              (symbol (str "my-" (plural n)))
              (fn [] (filter type-predicate (.getMyUnits api)))))))

(def my-citadels-of-adun my-citadel-of-aduns)
(def my-nexuses my-nexus)

(defn get-unit-by-id [unit-id] (.getUnit api unit-id))

(defn my-buildings [] (filter building? (my-units)))

;; API unit commands

(defn attack
  ([attacking-unit target-unit] (.attack api (.getID attacking-unit) (.getID target-unit)))
  ([attacking-unit px py] (.attack api (.getID attacking-unit) px py)))

(defn build
  ([builder point to-build] (build builder (.x point) (.y point) to-build))
  ([builder tx ty to-build] (.build api (.getID builder) tx ty
                                    (.getID (to-build unit-type-kws)))))

(defn build-addon [building to-build]
  (.buildAddon api (.getID building) (.getID (to-build unit-type-kws))))

(defn train [building to-train]
  (.train api (.getID building) (.getID (to-train unit-type-kws))))

(defn morph [unit morph-to]
  (.morph api (.getID unit) (.getID (morph-to unit-type-kws))))

(defn research [unit to-research]
  (.research api (.getID unit) (.getID (to-research tech-type-kws))))

(defn upgrade [unit to-upgrade]
  (.upgrade api (.getID unit) (.getID (to-upgrade upgrade-type-kws))))

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

;; API utility and drawing commands

(defn start [] (.start api))

(defn load-type-data [] (.loadTypeData api))

(defn replay-frame-total [] (.getReplayFrameTotal api))

(defn draw-health [boolean] (.drawHealth api boolean))

(defn draw-targets [boolean] (.drawTargets api boolean))

(defn draw-ids [boolean] (.drawIDs api boolean))

(defn enable-user-input [] (.enableUserInput api))

(defn enable-perfect-information [] (.enablePerfectInformation api))

(defn set-game-speed [speed] (.setGameSpeed api speed))

(defn frame-count [] (.getFrameCount api))

(defn set-frame-skip [frame-skip] (.setFrameSkip api frame-skip))

(defn leave-game [] (.leaveGame api))

(defn draw-box [left top right bottom color fill screen-coords]
  (.drawBox api left top right bottom color fill screen-coords))

(defn draw-circle [px py radius color fill screen-coords]
  (.drawCircle api px py radius color fill screen-coords))

(defn draw-line
  ([p1 p2 color screen-coords] (.drawLine api p1 p2 color screen-coords))
  ([x1 y1 x2 y2 color screen-coords] (.drawLine api x1 y1 x2 y2 color screen-coords)))

(defn draw-dot [px py color screen-coords]
  (.drawDot api px py color screen-coords))

(defn draw-text
  ([point msg screen-coords] (.drawText api point (str msg) screen-coords))
  ([px py msg screen-coords] (.drawText api px py (str msg) screen-coords)))

;; extended API commands

(defn tile-visible?
  ([point] (tile-visible? (.x point) (.y point)))
  ([tx ty] (.isVisible api tx ty)))

(defn tile-explored?
  ([point] (tile-explored? (.x point) (.y point)))
  ([tx ty] (.isExplored api tx ty)))

(defn tile-buildable?
  ([point include-buildings] (tile-buildable? (.x point) (.y point) include-buildings))
  ([tx ty include-buildings] (.isBuildable api tx ty include-buildings)))

(defn has-creep?
  ([point] (has-creep? (.x point) (.y point)))
  ([tx ty] (.hasCreep api tx ty)))

(defn- has-power?*
  ([tx ty] (.hasPower api tx ty))
  ([tx ty unit] (.hasPower api tx ty (get-type-id unit)))
  ([tx ty tile-width tile-height] (.hasPower api tx ty tile-width tile-height))
  ([tx ty tile-width tile-height unit] (.hasPower api tx ty tile-width tile-height (get-type-id unit))))

(defn has-power? [point-or-coord & rest-args]
  (cond
   (instance? java.awt.Point point-or-coord) (apply has-power?*
                                                    (.x point-or-coord)
                                                    (.y point-or-coord)
                                                    rest-args)
   :else (apply has-power?* point-or-coord rest)))

(defn has-power-precise?
  ([point] (has-power-precise? (.x point) (.y point)))
  ([px py] (.hasPowerPrecise api px py)))

(defn has-path?
  ([unit target-unit] (.hasPath api (.getID unit) (.getID target-unit)))
  ([unit to-x to-y] (.hasPath api (.getID unit) to-x to-y))
  ([from-x from-y to-x to-y] (.hasPath api from-x from-y to-x to-y)))

(defn has-loaded-unit? [unit maybe-loaded-unit]
  (.hasLoadedUnit api (.getID unit) (.getID maybe-loaded-unit)))

(defn can-build-here?
  ([tx ty unit-to-build check-explored] (.canBuildHere api tx ty (get-type-id unit-to-build) check-explored))
  ([unit tx ty unit-to-build check-explored] (.canBuildHere api (.getID unit) tx ty
                                                            (get-type-id unit-to-build) check-explored)))

(defn can-make?
  ([unit-to-make] (.canMake api (get-type-id unit-to-make)))
  ([unit unit-to-make] (.canMake api (.getID unit) (get-type-id unit-to-make))))

(defn can-research?
  ([tech] (.canResearch api (get-type-id tech)))
  ([unit tech] (.canResearch api (.getID unit) (get-type-id tech))))

(defn can-upgrade?
  ([upgrade] (.canUpgrade api (get-type-id upgrade)))
  ([unit upgrade] (.canUpgrade api (.getID unit) (get-type-id upgrade))))

(defn print-text [msg] (.printText api msg))

(defn send-text [msg] (.sendText api msg))

(defn set-command-optimization-level [level] (.setCommandOptimizationLevel api level))

(defn replay? [] (.isReplay api))

(defn visible-to-player? [unit player] (.isVisibleToPlayer api (.getID unit) (.getID player)))

(defn last-error [] (.getLastError api))

(defn remaining-latency-frames [] (.getRemainingLatencyFrames api))

(defn units-on-tile
  ([point] (units-on-tile (.x point) (.y point)))
  ([tx ty] (.getUnitsOnTile api tx ty)))

;; utility functions supplemental to JNIBWAPI

(defn dist [a b]
  (Math/sqrt (+ (Math/pow (- (pixel-x a) (pixel-x b)) 2) (Math/pow (- (pixel-y a) (pixel-y b)) 2))))

(defn dist-tile [a b]
  (Math/sqrt (+ (Math/pow (- (tile-x a) (tile-x b)) 2) (Math/pow (- (tile-y a) (tile-y b)) 2))))
