(ns korhal.interop
  (:import (jnibwapi.model.Unit)
           (jnibwapi.types.UnitType$UnitTypes)))

(declare get-type)

(def ^:dynamic api nil)
(defn bind-api [binding] (alter-var-root (var api) #(identity %2) binding))

;; type definitions

(def unit-types
  ['marine 'Terran_Marine
   'ghost 'Terran_Ghost
   'vulture 'Terran_Vulture
   'goliath-turret 'Undefined4
   'siege-tank-tank-mode 'Terran_Siege_Tank_Tank_Mode
   'siege-tank-turret-tank-mode 'Undefined6
   'scv 'Terran_SCV
   'wraith 'Terran_Wraith
   'science-vessel 'Terran_Science_Vessel
   'dropship 'Terran_Dropship
   'battlecruiser 'Terran_Battlecruiser
   'spider-mine 'Terran_Vulture_Spider_Mine
   'nuclear-missile 'Terran_Nuclear_Missile
   'siege-tank-siege-mode 'Terran_Siege_Tank_Siege_Mode
   'siege-tank-turret-siege-mode 'Undefined31
   'firebat 'Terran_Firebat
   'scanner-sweep 'Spell_Scanner_Sweep
   'medic 'Terran_Medic
   'larva 'Zerg_Larva
   'egg 'Zerg_Egg
   'zergling 'Zerg_Zergling
   'hydralisk 'Zerg_Hydralisk
   'ultralisk 'Zerg_Ultralisk
   'broodling 'Zerg_Broodling
   'drone 'Zerg_Drone
   'overlord 'Zerg_Overlord
   'mutalisk 'Zerg_Mutalisk
   'guardian 'Zerg_Guardian
   'queen 'Zerg_Queen
   'defiler 'Zerg_Defiler
   'scourge 'Zerg_Scourge
   'infested-terran 'Zerg_Infested_Terran
   'valkyrie 'Terran_Valkyrie
   'cocoon 'Zerg_Cocoon
   'corsair 'Protoss_Corsair
   'dark-templar 'Protoss_Dark_Templar
   'devourer 'Zerg_Devourer
   'dark-archon 'Protoss_Dark_Archon
   'probe 'Protoss_Probe
   'zealot 'Protoss_Zealot
   'dragoon 'Protoss_Dragoon
   'high-templar 'Protoss_High_Templar
   'archon 'Protoss_Archon
   'shuttle 'Protoss_Shuttle
   'scout 'Protoss_Scout
   'arbiter 'Protoss_Arbiter
   'carrier 'Protoss_Carrier
   'interceptor 'Protoss_Interceptor
   'reaver 'Protoss_Reaver
   'observer 'Protoss_Observer
   'scarab 'Protoss_Scarab
   'rhynadon 'Critter_Rhynadon
   'bengalaas 'Critter_Bengalaas
   'scantid 'Critter_Scantid
   'kakaru 'Critter_Kakaru
   'ragnasaur 'Critter_Ragnasaur
   'ursadon 'Critter_Ursadon
   'lurker-egg 'Zerg_Lurker_Egg
   'lurker 'Zerg_Lurker
   'disruption-web 'Spell_Disruption_Web
   'command-center 'Terran_Command_Center
   'comsat-station 'Terran_Comsat_Station
   'nuclear-silo 'Terran_Nuclear_Silo
   'supply-depot 'Terran_Supply_Depot
   'refinery 'Terran_Refinery
   'barracks 'Terran_Barracks
   'academy 'Terran_Academy
   'factory 'Terran_Factory
   'starport 'Terran_Starport
   'control-tower 'Terran_Control_Tower
   'science-facility 'Terran_Science_Facility
   'covert-ops 'Terran_Covert_Ops
   'physics-lab 'Terran_Physics_Lab
   'machine-shop 'Terran_Machine_Shop
   'engineering-bay 'Terran_Engineering_Bay
   'armory 'Terran_Armory
   'missile-turret 'Terran_Missile_Turret
   'bunker 'Terran_Bunker
   'infested-command-center 'Zerg_Infested_Command_Center
   'hatchery 'Zerg_Hatchery
   'lair 'Zerg_Lair
   'hive 'Zerg_Hive
   'nydus-canal 'Zerg_Nydus_Canal
   'hydralisk-den 'Zerg_Hydralisk_Den
   'defiler-mound 'Zerg_Defiler_Mound
   'greater-spire 'Zerg_Greater_Spire
   'queens-nest 'Zerg_Queens_Nest
   'evolution-chamber 'Zerg_Evolution_Chamber
   'ultralisk-cavern 'Zerg_Ultralisk_Cavern
   'spire 'Zerg_Spire
   'spawning-pool 'Zerg_Spawning_Pool
   'creep-colony 'Zerg_Creep_Colony
   'spore-colony 'Zerg_Spore_Colony
   'sunken-colony 'Zerg_Sunken_Colony
   'extractor 'Zerg_Extractor
   'nexus 'Protoss_Nexus
   'robotics-facility 'Protoss_Robotics_Facility
   'pylon 'Protoss_Pylon
   'assimilator 'Protoss_Assimilator
   'observatory 'Protoss_Observatory
   'gateway 'Protoss_Gateway
   'photon-cannon 'Protoss_Photon_Cannon
   'citadel-of-adun 'Protoss_Citadel_of_Adun
   'cybernetics-core 'Protoss_Cybernetics_Core
   'templar-archives 'Protoss_Templar_Archives
   'forge 'Protoss_Forge
   'stargate 'Protoss_Stargate
   'fleet-beacon 'Protoss_Fleet_Beacon
   'arbiter-tribunal 'Protoss_Arbiter_Tribunal
   'robotics-support-bay 'Protoss_Robotics_Support_Bay
   'shield-battery 'Protoss_Shield_Battery])

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
          (merge {:mineral 'Resource_Mineral_Field :geyser 'Resource_Vespene_Geyser}
                 (zipmap (map keyword (take-nth 2 unit-types)) (take-nth 2 (rest unit-types))))))

(gen-type-kw-lookup)

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

(defmacro define-unit-type-fns []
  (let [dynamic-dot-form (fn [instance method] `(. ~instance ~method))
        unit-maps ['get-name 'getName
                   'race-id 'getRaceID
                   'what-build-id 'getWhatBuildID
                   'armor-upgrade-id 'getArmorUpgradeID
                   'max-hit-points 'getMaxHitPoints
                   'max-shields 'getMaxShields
                   'max-energy 'getMaxEnergy
                   'armor 'getArmor
                   'mineral-price 'getMineralPrice
                   'gas-price 'getGasPrice
                   'build-time 'getBuildTime
                   'supply-required 'getSupplyRequired
                   'supply-provided 'getSupplyProvided
                   'space-required 'getSpaceRequired
                   'space-provided 'getSpaceProvided
                   'build-score 'getBuildScore
                   'destroy-score 'getDestroyScore
                   'size-id 'getSizeID
                   'tile-width 'getTileWidth
                   'tile-height 'getTileHeight
                   'dimension-left 'getDimensionLeft
                   'dimension-up 'getDimensionUp
                   'dimension-right 'getDimensionRight
                   'dimension-down 'getDimensionDown
                   'seek-range 'getSeekRange
                   'sight-range 'getSightRange
                   'ground-weapon-id 'getGroundWeaponID
                   'max-ground-hits 'getMaxGroundHits
                   'air-weapon-id 'getAirWeaponID
                   'max-air-hits 'getMaxAirHits
                   'top-speed 'getTopSpeed
                   'acceleration 'getAcceleration
                   'halt-distance 'getHaltDistance
                   'turn-radius 'getTurnRadius
                   'produce-capable? 'isProduceCapable
                   'attack-capable? 'isAttackCapable
                   'can-move? 'isCanMove
                   'flyer? 'isFlyer
                   'regenerates? 'isRegenerates
                   'spellcaster? 'isSpellcaster
                   'invincible? 'isInvincible
                   'organic? 'isOrganic
                   'mechanical? 'isMechanical
                   'robotic? 'isRobotic
                   'detector? 'isDetector
                   'resource-container? 'isResourceContainer
                   'refinery? 'isRefinery
                   'worker? 'isWorker
                   'requires-psi? 'isRequiresPsi
                   'requires-creep? 'isRequiresCreep
                   'burrowable? 'isBurrowable
                   'cloakable? 'isCloakable
                   'building? 'isBuilding
                   'addon? 'isAddon
                   'flying-building? 'isFlyingBuilding
                   'spell? 'isSpell]]
    (cons `do
          (for [[clj-name java-name] (partition 2 unit-maps)]
            `(defn ~clj-name [unit#] (. (get-type unit#) ~java-name))))))

(define-unit-type-fns)

(defmacro define-unit-fns []
  (let [dynamic-dot-form (fn [instance method] `(. ~instance ~method))
        unit-maps ['replay-id 'getReplayID
                   'player-id 'getPlayerID
                   'type-id 'getTypeID
                   'pixel-x 'getX
                   'pixel-y 'getY
                   'tile-x 'getTileX
                   'tile-y 'getTileY
                   'angle 'getAngle
                   'velocity-x 'getVelocityX
                   'velocity-y 'getVelocityY
                   'hit-points 'getHitPoints
                   'shields 'getShields
                   'energy 'getEnergy
                   'resources 'getResources
                   'resource-group 'getResourceGroup
                   'last-command-frame 'getLastCommandFrame
                   'last-command-id 'getLastCommandID
                   'initial-x 'getInitialX
                   'initial-y 'getInitialY
                   'initial-tile-x 'getInitialTileX
                   'initial-tile-y 'getInitialTileY
                   'initial-hit-points 'getInitialHitPoints
                   'initial-resources 'getInitialResources
                   'kill-count 'getKillCount
                   'acid-spore-count 'getAcidSporeCount
                   'interceptor-count 'getInterceptorCount
                   'scarab-count 'getScarabCount
                   'spider-mine-count 'getSpiderMineCount
                   'ground-weapon-cooldown 'getGroundWeaponCooldown
                   'air-weapon-cooldown 'getAirWeaponCooldown
                   'spell-cooldown 'getSpellCooldown
                   'defense-matrix-points 'getDefenseMatrixPoints
                   'defense-matrix-timer 'getDefenseMatrixTimer
                   'ensnare-timer 'getEnsnareTimer
                   'irradiate-timer 'getIrradiateTimer
                   'lockdown-timer 'getLockdownTimer
                   'maelstrom-timer 'getMaelstromTimer
                   'order-timer 'getOrderTimer
                   'plague-timer 'getPlagueTimer
                   'remove-timer 'getRemoveTimer
                   'statis-timer 'getStasisTimer
                   'stim-timer 'getStimTimer
                   'build-type-id 'getBuildTypeID
                   'training-queue-size 'getTrainingQueueSize
                   'researching-tech-id 'getResearchingTechID
                   'upgrading-upgrade-id 'getUpgradingUpgradeID
                   'remaining-build-timer 'getRemainingBuildTimer
                   'remaining-train-time 'getRemainingTrainTime
                   'remaining-research-time 'getRemainingResearchTime
                   'remaining-upgrade-time 'getRemainingUpgradeTime
                   'build-unit-id 'getBuildUnitID
                   'target-unit-id 'getTargetUnitID
                   'target-x 'getTargetX
                   'target-y 'getTargetY
                   'order-id 'getOrderID
                   'order-target-id 'getOrderTargetID
                   'secondary-order-id 'getSecondaryOrderID
                   'rally-x 'getRallyX
                   'rally-y 'getRallyY
                   'rally-unit-id 'getRallyUnitID
                   'addon-id 'getAddOnID
                   'transport-id 'getTransportID
                   'num-loaded-units 'getNumLoadedUnits
                   'num-larva 'getNumLarva
                   'is-exists? 'isExists
                   'nuke-ready? 'isNukeReady
                   'accelerating? 'isAccelerating
                   'attacking? 'isAttacking
                   'attack-frame? 'isAttackFrame
                   'being-constructed? 'isBeingConstructed
                   'being-gathered? 'isBeingGathered
                   'being-healed? 'isBeingHealed
                   'blind? 'isBlind
                   'braking? 'isBraking
                   'burrowed? 'isBurrowed
                   'carrying-gas? 'isCarryingGas
                   'carrying-minerals? 'isCarryingMinerals
                   'cloaked? 'isCloaked
                   'completed? 'isCompleted
                   'constructing? 'isConstructing
                   'defense-matrixed? 'isDefenseMatrixed
                   'detected? 'isDetected
                   'ensnared? 'isEnsnared
                   'following? 'isFollowing
                   'gathering-gas? 'isGatheringGas
                   'gathering-minerals? 'isGatheringMinerals
                   'hallucination? 'isHallucination
                   'holding-position? 'isHoldingPosition
                   'idle? 'isIdle
                   'interruptible? 'isInterruptable
                   'invincible? 'isInvincible
                   'irradiated? 'isIrradiated
                   'lifted? 'isLifted
                   'loaded? 'isLoaded
                   'locked-down? 'isLockedDown
                   'maelstrommed? 'isMaelstrommed
                   'morphing? 'isMorphing
                   'moving? 'isMoving
                   'parasited? 'isParasited
                   'patrolling? 'isPatrolling
                   'plagued? 'isPlagued
                   'repairing? 'isRepairing
                   'selected? 'isSelected
                   'sieged? 'isSieged
                   'starting-attack? 'isStartingAttack
                   'statised? 'isStasised
                   'stimmed? 'isStimmed
                   'stuck? 'isStuck
                   'training? 'isTraining
                   'under-attack? 'isUnderAttack
                   'under-dark-swarm? 'isUnderDarkSwarm
                   'under-disruption-web? 'isUnderDisruptionWeb
                   'under-storm? 'isUnderStorm
                   'unpowered? 'isUnpowered
                   'upgrading? 'isUpgrading
                   'visible? 'isVisible]]
    (cons `do
          (for [[clj-name java-name] (partition 2 unit-maps)]
            `(defn ~clj-name [unit#] (. unit# ~java-name))))))

(define-unit-fns)

;; common API commands shared among multiple types

(defn get-id [obj] (.getID obj))

(defn get-type [unit] (.getUnitType api (.getTypeID unit)))

(defn right-click [selected target]
  (.rightClick api (.getID selected) (.getID target)))

;; unit commands

(defn attack [unit target]
  (.attack api (.getID unit) (.getX target) (.getY target)))

(defn build [builder tile-x tile-y to-build]
  (.build api (.getID builder) tile-x tile-y
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(to-build type-kw-lookup))))))

(defn morph [unit morph-to]
  (.morph api
          (.getID unit)
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(morph-to type-kw-lookup))))))

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

;; utility functions

(defn swap-key [curr-val k v]
  (merge curr-val {k v}))

(defn swap-keys [swap-atom & forms]
  (doseq [[k v] (partition 2 forms)]
    (swap! swap-atom swap-key k v)))

(defn dist [a b]
  (Math/sqrt (+ (Math/pow (- (.getX a) (.getX b)) 2) (Math/pow (- (.getY a) (.getY b)) 2))))
