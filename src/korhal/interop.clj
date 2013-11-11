(ns korhal.interop
  (:import (jnibwapi.model.Unit)
           (jnibwapi.types.UnitType$UnitTypes)))

(declare get-type)

(def ^:dynamic api nil)
(defn bind-api [binding] (alter-var-root (var api) #(identity %2) binding))

;; type definitions

(def unit-types
  ['larva 'Zerg_Larva
   'drone 'Zerg_Drone
   'overlord 'Zerg_Overlord
   'zergling 'Zerg_Zergling])

(defn gen-type-id-lookup []
  (intern *ns*
          (symbol 'type-ids)
          (->> (map #(vector (eval `(.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                             (eval (symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                   (take-nth 2 (rest unit-types)))
               (flatten)
               (apply hash-map))))
(gen-type-id-lookup)

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

;; generate single unit functions

(defn is-idle? [obj] (.isIdle obj))

(defmacro define-unit-type-fns []
  (let [dynamic-dot-form (fn [instance method] `(. ~instance ~method))
        unit-maps ['get-race 'getRace
                   'max-hit-points 'maxHitPoints
                   'max-shields 'maxShields
                   'max-energy 'maxEnergy
                   'armor 'armor
                   'mineral-price 'mineralPrice
                   'gas-price 'gasPrice
                   'build-time 'buildTime
                   'supply-required 'supplyRequired
                   'supply-provided 'supplyProvided
                   'space-required 'spaceRequired
                   'space-provided 'spaceProvided
                   'build-score 'buildScore
                   'destroy-score 'destroyScore
                   'size 'size
                   'tile-width 'tileWidth
                   'tile-height 'tileHeight
                   'dimension-left 'dimensionLeft
                   'dimension-up 'dimensionUp
                   'dimension-right 'dimensionRight
                   'dimension-down 'dimensionDown
                   'seek-range 'seekRange
                   'sight-range 'sightRange
                   'ground-weapon 'groundWeapon
                   'max-grounds-hits 'maxGroundHits
                   'air-weapon 'airWeapon
                   'max-air-hits 'maxAirHits
                   'top-speed 'topSpeed
                   'acceleration 'acceleration
                   'halt-distance 'haltDistance
                   'turn-radius? 'turnRadius
                   'can-produce? 'canProduce
                   'can-attack? 'canAttack
                   'can-move? 'canMove
                   'is-flyer? 'isFlyer
                   'regenerates-hp? 'regeneratesHP
                   'has-permanent-cloak? 'hasPermanentCloak
                   'is-invincible? 'isInvincible
                   'is-organic? 'isOrganic
                   'is-mechanical? 'isMechanical
                   'is-robotic? 'isRobotic
                   'is-detector? 'isDetector
                   'is-resource-container? 'isResourceContainer
                   'is-resource-depot? 'isResourceDepot
                   'is-refinery? 'isRefinery
                   'is-worker? 'isWorker
                   'requires-psi? 'requiresPsi
                   'requires-creep? 'requiresCreep
                   'is-two-units-in-one-egg? 'isTwoUnitsInOneEgg
                   'is-burrowable? 'isBurrowable
                   'is-cloakable? 'isCloakable
                   'is-building? 'isBuilding
                   'is-addon? 'isAddon
                   'is-flying-building? 'isFlyingBuilding
                   'is-neutral? 'isNeutral
                   'is-hero? 'isHero
                   'is-powerup? 'isPowerup
                   'is-beacon? 'isBeacon
                   'is-flag-beacon? 'isFlagBeacon
                   'is-special-building? 'isSpecialBuilding
                   'is-spell? 'isSpell
                   'produces-larva? 'producesLarva
                   'is-mineral-field? 'isMineralField
                   'can-build-addon? 'canBuildAddon]]
    (cons `do
          (for [[clj-name java-name] (partition 2 unit-maps)]
            `(defn ~clj-name [unit#] (. (get-type unit#) ~java-name))))))

(define-unit-type-fns)

;; common API commands

(defn get-id [obj] (.getID obj))

(defn get-type [unit] (.getUnitType api (.getTypeID unit)))

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
