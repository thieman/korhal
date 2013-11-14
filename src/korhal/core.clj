(ns korhal.core
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.macro.engine :refer [start-macro-engine run-macro-engine]]
            [korhal.tools.util :refer [swap-key swap-keys plural]]
            [korhal.tools.contract :refer [available-minerals available-gas
                                           contract-build contracted-max-supply
                                           clear-contracts cancel-contracts
                                           show-contract-display clear-contract-atoms
                                           can-build?]])
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
  (clear-contract-atoms)
  (start-macro-engine))

(defn korhal-gameUpdate [this]
  (clear-contracts)
  (run-macro-engine))

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
  ;; NOTE: destroyed units are no longer available through the API
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
