(ns korhal.core
  (:require [clojure.tools.nrepl.server :as repl]
            [korhal.interop.interop :refer :all]
            [korhal.macro.state :refer [start-macro-engine]]
            [korhal.macro.engine :refer [run-macro-engine]]
            [korhal.macro.state :refer [builder-to-constructor!
                                        construction-completed!]]
            [korhal.micro.engine :refer [start-micro-engine run-micro-engine
                                         micro-tag-new-unit!]]
            [korhal.tools.util :refer [swap-key swap-keys profile]]
            [korhal.tools.contract :refer [available-minerals available-gas
                                           contract-build contracted-max-supply
                                           clear-contracts cancel-contracts
                                           show-contract-display clear-contract-atoms
                                           can-build? contract-add-initial-cc
                                           contract-add-new-building]])
  (:import (clojure.lang.IDeref)
           (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)))

(def repl-server (atom nil))
(def repl-command (atom nil))
(def repl-result (atom nil))

(defmacro cmd
  "This macro should be used from the REPL to call a command during
  the gameUpdate loop."
  [& body]
  `(do (reset! repl-command (fn [] (do ~@body)))
       (loop [result# nil]
         (if result#
           (let [to-display# (:result result#)]
             (reset! repl-result nil)
             to-display#)
           (recur @repl-result)))))

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
  (set-game-speed 10)
  (load-map-data true)
  (draw-targets true)
  (draw-ids true)
  (show-contract-display true)
  (clear-contract-atoms)
  (contract-add-initial-cc)
  (start-macro-engine)
  (start-micro-engine)
  (reset! repl-server (repl/start-server :port 7777)))

(defn korhal-gameUpdate [this]
  (when @repl-command
    (reset! repl-result {:result (@repl-command)})
    (reset! repl-command nil))
  (clear-contracts)
  (run-macro-engine)
  (run-micro-engine))

(defn korhal-gameEnded [this]
  (repl/stop-server @repl-server)
  (reset! repl-server nil))

(defn korhal-keyPressed [this keycode])

(defn korhal-matchEnded [this winner])
(defn korhal-sendText [this text])

(defn korhal-receiveText [this text])

(defn korhal-nukeDetect [this x y])
(defn korhal-playerLeft [this player-id])

(defn korhal-unitCreate [this unit-id]
  (let [unit (get-unit-by-id unit-id)]
    (when (my-unit? unit)
      (if (building? unit)
        (do (builder-to-constructor! unit)
            (contract-add-new-building unit))
        (micro-tag-new-unit! unit)))))

(defn korhal-unitDestroy [this unit-id]
  ;; NOTE: destroyed units are no longer available through the API
  (cancel-contracts unit-id))

(defn korhal-unitDiscover [this unit-id])

(defn korhal-unitEvade [this unit-id])

(defn korhal-unitHide [this unit-id])

(defn korhal-unitMorph [this unit-id]
  (let [unit (get-unit-by-id unit-id)]
    (when ((every-pred my-unit? is-refinery?) unit)
      (builder-to-constructor! unit)
      (contract-add-new-building unit))))

(defn korhal-unitShow [this unit-id])

(defn korhal-unitRenegade [this unit-id])

(defn korhal-saveGame [this game-name])

(defn korhal-unitComplete
  "BUG: This does NOT get called when a morphing unit (e.g. refinery)
  completes!"
  [this unit-id]
  (let [unit (get-unit-by-id unit-id)]
    (when ((every-pred my-unit? building?) unit)
      (construction-completed! unit))))

(defn korhal-playerDropped [this player-id])
