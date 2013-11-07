(ns korhal.core
  (:import (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)
           (jnibwapi.model.Unit)
           (jnibwapi.types.UnitType$UnitTypes)))

(gen-class
 :name "korhal.core"
 :implements [jnibwapi.BWAPIEventListener]
 :state state
 :init init
 :main true
 :constructors {[] []}
 :prefix "korhal-")

(defn swap-key [curr-val k v]
  (merge curr-val {k v}))

(defmacro swap-keys [swap-atom & forms]
  (for [pair (partition 2 forms)]
    `(swap! ~swap-atom swap-key ~@pair)))

(defn korhal-main [& args]
  (let [ai (korhal.core.)
        api (jnibwapi.JNIBWAPI. ai)]
    (swap! (.state ai) swap-key :api api)
    (.start (:api @(.state ai)))))

(defn korhal-init []
  [[] (atom {})])

(defn korhal-deref [this]
  @(.state this))

(defn korhal-connected [this]
  (.loadTypeData (:api @this)))

(defn korhal-gameStarted [this]
  (println "Here we go!")
  (doto (:api @this)
    (.enableUserInput)
    (.enablePerfectInformation)
    (.setGameSpeed 0)
    (.loadMapData true))
  (swap-keys (.state this)
    :claimed []
    :morphed-drone false
    :pool-drone -1
    :supply-cap 0))

(defn korhal-gameUpdate [this]

  (println "updating")

  ;; spawn a drone
  (for [unit (.getMyUnits (:api @this))]
    (when (= (.getTypeID unit) (.getID jnibwapi.types.UnitType$UnitTypes/Zerg_Larva))
      (when (and (>= (.. (:api @this) getSelf getMinerals) 50) (not (:morphed-drone this)))
        (println "morphing a drone")
        (.morph (:api @this) (.getID unit) (.getID jnibwapi.types.UnitType$UnitTypes/Zerg_Drone))
        (swap-keys (.state this) :morphed-drone true))))

  ;; collect minerals
  (for [unit (.getMyUnits (:api @this))]
    (when (= (.getTypeID unit) (.getID jnibwapi.types.UnitType$UnitTypes/Zerg_Drone))
      (when (and (.isIdle unit) (not (= (.getID unit) (:pool-drone @this))))
        (println "collecting minerals")
        (let [mineral? (fn [unit] (= (.getTypeID unit) (.getID jnibwapi.types.UnitType$UnitTypes/Resource_Mineral_Field)))
              mineral (first (filter mineral? (.getNeutralUnits (:api @this))))]
        (.rightClick (.getID unit) (.getID mineral)))))))



(defn korhal-gameEnded [this])
(defn korhal-keyPressed [this keycode])
(defn korhal-matchEnded [this winner])
(defn korhal-sendText [this text])
(defn korhal-receiveText [this text])
(defn korhal-nukeDetect [this x y])
(defn korhal-playerLeft [this playerID])
(defn korhal-unitCreate [this unitID])
(defn korhal-unitDestroy [this unitID])
(defn korhal-unitDiscover [this unitID])
(defn korhal-unitEvade [this unitID])
(defn korhal-unitHide [this unitID])
(defn korhal-unitMorph [this unitID])
(defn korhal-unitShow [this unitID])
(defn korhal-unitRenegade [this unitID])
(defn korhal-saveGame [this gameName])
(defn korhal-unitComplete [this unitID])
(defn korhal-playerDropped [this playerID])
