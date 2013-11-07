(ns korhal.core
  (:import (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)
           (jnibwapi.model.Unit)
           (jnibwapi.util.BWColor)))

(gen-class
 :name "korhal.core"
 :implements [jnibwapi.BWAPIEventListener]
 :state "state"
 :main true
 :constructors {[] []}
 :prefix "korhal-")

(defn korhal-main [& args] (korhal.core.))

(defn korhal-init [this] (.start (jnibwapi.JNIBWAPI. this)))

(defn korhal-connected [this])
(defn korhal-gameStarted [this])
(defn korhal-gameUpdate [this])
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
