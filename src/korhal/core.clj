(ns korhal.core
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.tools.util :refer [swap-key swap-keys plural]])
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
  (set-game-speed 0)
  (load-map-data true))

(defn korhal-gameUpdate [this]

  ;; train scvs
  (doseq [cc (filter #(zero? (training-queue-size %)) (my-command-centers))]
    (train cc :scv))

  ;; collect minerals
  (doseq [idle-scv (filter idle? (my-scvs))]
    (let [close-minerals (filter #(< (dist idle-scv %) 300) (minerals))]
      (right-click idle-scv (nth close-minerals (rand-int (count close-minerals)))))))

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
