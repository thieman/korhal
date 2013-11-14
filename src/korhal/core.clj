(ns korhal.core
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.tools.util :refer [swap-key swap-keys plural]]
            [korhal.tools.contract :refer [available-minerals available-gas
                                           contract-build contracted-max-supply
                                           clear-contracts cancel-contracts
                                           show-contract-display clear-contract-atoms]])
  (:import (clojure.lang.IDeref)
           (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)))

(gen-class
 :name "korhal.core"
 :implements [jnibwapi.BWAPIEventListener clojure.lang.IDeref]
 :state state
 :init init
 :main true
 :constructors {[] []}
 :prefix "korhal-")

(defn korhal-deref [this] @(.state this))

(defn korhal-main [& args]
  (let [ai (korhal.core.)
        api (jnibwapi.JNIBWAPI. ai)]
    (swap! (.state ai) swap-key :api api)
    (bind-api! api)
    (start)))

(defn korhal-init []
  [[] (atom {})])

(defn korhal-connected [this]
  (load-type-data))

(defn korhal-gameStarted [this]
  (println "Game Started")
  (enable-user-input)
  (set-game-speed 5)
  (load-map-data true)
  (draw-targets true)
  (draw-ids true)
  (show-contract-display true)
  (clear-contract-atoms))

(defn korhal-gameUpdate [this]

  (clear-contracts)

  ;; train scvs
  (doseq [cc (filter #(zero? (training-queue-size %)) (my-command-centers))]
    (when (>= (available-minerals) 50)
      (train cc :scv)))

  ;; collect minerals
  (doseq [idle-scv (filter idle? (my-scvs))]
    (let [closest-mineral (apply min-key (partial dist idle-scv) (minerals))]
      (cancel-contracts idle-scv)
      (right-click idle-scv closest-mineral)))

  ;; build supply depots
  (when (and (>= (+ (my-supply-used) 200) (contracted-max-supply))
             (>= (available-minerals) 100))
    (let [cc (first (my-command-centers))
          builder (first (filter gathering-minerals? (my-scvs)))]
      (loop [attempt 0]
        (when-not (>= attempt 5)
          (let [tx (+ (tile-x cc) (* (Math/pow -1 (rand-int 2)) (rand-int 20)))
                ty (+ (tile-y cc) (* (Math/pow -1 (rand-int 2)) (rand-int 20)))]
            (if (can-build? tx ty :supply-depot true)
              (do (cancel-contracts builder)
                  (contract-build builder tx ty :supply-depot))
              (recur (inc attempt)))))))))

(defn korhal-gameEnded [this])
(defn korhal-keyPressed [this keycode])
(defn korhal-matchEnded [this winner])
(defn korhal-sendText [this text])

(defn korhal-receiveText [this text]
  (println (str "RECEIVED: " text)))

(defn korhal-nukeDetect [this x y])
(defn korhal-playerLeft [this player-id])

(defn korhal-unitCreate [this unit-id]
  (println (str "CREATED: " unit-id)))

(defn korhal-unitDestroy [this unit-id]
  (println (str "DESTROYED: " unit-id))
  (cancel-contracts unit-id))

(defn korhal-unitDiscover [this unit-id]
  (println (str "DISCOVERED: " unit-id)))

(defn korhal-unitEvade [this unit-id]
  (println (str "EVADED: " unit-id)))

(defn korhal-unitHide [this unit-id]
  (println (str "HIDE: " unit-id)))

(defn korhal-unitMorph [this unit-id]
  (println (str "MORPH: " unit-id)))

(defn korhal-unitShow [this unit-id]
  (println (str "SHOW: " unit-id)))

(defn korhal-unitRenegade [this unit-id]
  (println (str "RENEGADE: " unit-id)))

(defn korhal-saveGame [this game-name])

(defn korhal-unitComplete [this unit-id]
  (println (str "COMPLETE: " unit-id)))

(defn korhal-playerDropped [this player-id])
