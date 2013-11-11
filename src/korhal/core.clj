(ns korhal.core
  (:require [korhal.interop :refer :all])
  (:import (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)))

(gen-class
 :name "korhal.core"
 :implements [jnibwapi.BWAPIEventListener]
 :state state
 :init init
 :main true
 :constructors {[] []}
 :prefix "korhal-")

(defn korhal-main [& args]
  (let [ai (korhal.core.)
        api (jnibwapi.JNIBWAPI. ai)]
    (swap! (.state ai) swap-key :api api)
    (bind-api api)
    (.start (:api @(.state ai)))))

(defn korhal-init []
  [[] (atom {})])

(defn korhal-connected [this]
  (.loadTypeData (:api @(.state this))))

(defn korhal-gameStarted [this]
  (println "Game Started")
  (doto (:api @(.state this))
    (.enableUserInput)
    (.enablePerfectInformation)
    (.setGameSpeed 0)
    (.loadMapData true))
  (swap-keys (.state this)
    :claimed []
    :pool-drone -1
    :spawning-pool-started false
    :overlord-spawned false))

(defn korhal-gameUpdate [this]

  ;; spawn drones
  (doseq [larva (my-larvas)]
    (when (and (>= (my-minerals) 50) (= (count (my-drones)) 4))
      (morph larva :drone)))

  ;; collect minerals
  (doseq [drone (my-drones)]
    (when (and (idle? drone)
               (not (= (get-id drone) (:pool-drone @(.state this)))))
      (right-click drone (first (filter #(< (dist drone %) 300) (minerals))))))

  ;; build a spawning pool
  (when (and (>= (my-minerals) 200) (< (:pool-drone @(.state this)) 0))
    (let [pool-drone (first (my-drones))
          overlord (first (my-overlords))
          build-x (if (< (tile-x overlord) 40) (+ (tile-x overlord) 2) (- (tile-x overlord) 2))]
      (swap-keys (.state this)
                 :pool-drone (get-id pool-drone)
                 :spawning-pool-started true)
      (build pool-drone build-x (tile-y overlord) :spawning-pool)))

  ;; spawn overlords
  (when (and (>= (my-supply-used) (- (my-supply-total) 3))
             (>= (my-minerals) 100)
             (not (:overlord-spawned @(.state this)))
             (:spawning-pool-started @(.state this)))
      (morph (first (my-larvas)) :overlord)
      (swap-keys (.state this) :overlord-spawned true))

  ;; spawn zerglings
  (when (>= (my-minerals) 50)
    (morph (first (my-larvas)) :zergling))

  ;; attack
  (doseq [zergling (my-zerglings)]
    (when (idle? zergling)
      (attack zergling (first (enemy-units))))))

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
