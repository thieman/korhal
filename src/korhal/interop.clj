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
