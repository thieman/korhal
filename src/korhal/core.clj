(ns korhal.core
  (:require [korhal.interop.interop :refer :all]
            [korhal.strategy.engine :refer [start-strategy-engine! stop-strategy-engine!
                                            strategy-inform! strategy-expire!
                                            strategy-remove!]]
            [korhal.macro.engine :refer [start-macro-engine! stop-macro-engine!]]
            [korhal.macro.state :refer [builder-to-constructor!
                                        construction-completed!]]
            [korhal.micro.engine :refer [start-micro-engine! stop-micro-engine!
                                         micro-tag-new-unit!]]
            [korhal.tools.util :refer [swap-key swap-keys profile]]
            [korhal.tools.repl :refer :all]
            [korhal.tools.queue :refer :all]
            [korhal.tools.contract :refer [available-minerals available-gas
                                           contract-build contracted-max-supply
                                           clear-contracts cancel-contracts
                                           show-contract-display clear-contract-atoms
                                           can-build? contract-add-initial-cc
                                           contract-add-new-building
                                           contract-display draw-contract-display]])
  (:import (clojure.lang.IDeref)
           (jnibwapi.JNIBWAPI)
           (jnibwapi.BWAPIEventListener)))

(def run-repl? true)

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
  (when run-repl? (start-repl! 7777)))

(defn korhal-gameUpdate [this]
  ;; we have to do this here instead of korhal-gameStarted because frame does not
  ;; get reset to 0 until now when restarting a game
  (when (zero? (frame-count))
    (clear-contract-atoms)
    (when (seq (my-command-centers)) (contract-add-initial-cc))
    (start-strategy-engine!)
    (start-macro-engine!)
    (start-micro-engine!))
  (when @contract-display (draw-contract-display))
  (strategy-expire! :nukes 300) ;; estimated frames for a nuke to drop
  (execute-api-queue)
  (execute-repl-queue))

(defn korhal-gameEnded [this]
  (stop-strategy-engine!)
  (stop-macro-engine!)
  (stop-micro-engine!)
  (when run-repl? (stop-repl!)))

(defn korhal-keyPressed [this keycode])

(defn korhal-matchEnded [this winner])
(defn korhal-sendText [this text])

(defn korhal-receiveText [this text])

(defn korhal-nukeDetect [this x y]
  (strategy-inform! :nukes {:x x :y y :frame (frame-count)}))

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
  (cancel-contracts unit-id)
  (strategy-remove! :enemy-units unit-id))

(defn korhal-unitDiscover [this unit-id]
  (let [unit (get-unit-by-id unit-id)]
    (when (not (my-unit? unit))
      (strategy-inform! :enemy-units {:id unit-id
                                      :type (get-unit-type unit)
                                      :x (pixel-x unit)
                                      :y (pixel-y unit)
                                      :frame (frame-count)}))))

(defn korhal-unitEvade [this unit-id])

(defn korhal-unitHide [this unit-id])

(defn korhal-unitMorph [this unit-id]
  (let [unit (get-unit-by-id unit-id)]
    (when ((every-pred my-unit? is-refinery?) unit)
      (builder-to-constructor! unit)
      (contract-add-new-building unit))))

(defn korhal-unitShow [this unit-id]
  (let [unit (get-unit-by-id unit-id)]
    (when (not (my-unit? unit))
      (strategy-inform! :enemy-units {:id unit-id
                                      :type (get-unit-type unit)
                                      :x (pixel-x unit)
                                      :y (pixel-y unit)
                                      :frame (frame-count)}))))

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
