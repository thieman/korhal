(ns korhal.strategy.engine
  (:require [clojure.set :refer [map-invert union difference]]
            [korhal.interop.interop :refer :all]
            [korhal.strategy.query :refer [strategy-state]]
            [korhal.micro.state :refer [micro-tag-unit!]]
            [korhal.micro.combat :refer [locked-down?*]]
            [korhal.tools.repl :refer [repl-control]]
            [korhal.tools.queue :refer [with-api]]))

(defn strategy-inform! [tag-type doc]
  (dosync
   (if (:id doc)
     (commute strategy-state assoc-in [tag-type (:id doc)] doc)
     (commute strategy-state update-in [tag-type] conj doc))))

(defn strategy-remove! [tag-type id]
  (dosync
   (commute strategy-state update-in [tag-type] #(dissoc % id))))

(defn strategy-expire! [tag-type frame-diff]
  (dosync
   (let [current (@strategy-state tag-type)
         keep (for [[k v] current
                    :when (not (>= (- (frame-count) (:frame v)) frame-diff))]
                k)]
     (commute strategy-state update-in [tag-type] select-keys keep))))

(defn draw-squads-display []
  (let [squads (:squad-members @strategy-state)]
    (doseq [unit (my-units)]
      (when-let [squad (squads unit)]
        (draw-text (pixel-x unit) (pixel-y unit) squad false)))))

(defn reform-squads []
  (loop [latest-id 0
         squads {}
         rest' (remove #(or (worker? %) (building? %)) (my-units))]
    (if-not (seq rest')
      (dosync
       (commute strategy-state assoc :squad-members squads))
      (let [unit (first rest')
            same-type (fn [target] (= (get-unit-type-kw unit) (get-unit-type-kw target)))
            closest-same-type-ally (closest unit (filter same-type (keys squads)))
            ally-squad (squads closest-same-type-ally)]
        (if (and ally-squad (<= (dist unit closest-same-type-ally) 300))
          (recur latest-id (assoc squads unit ally-squad) (next rest'))
          (recur (inc latest-id) (assoc squads unit latest-id) (next rest')))))))

(defn tag-squads []
  (let [state @strategy-state]
    (doseq [squad (keys (map-invert (:squad-members state)))]
      (let [leader (first (filter #(= squad ((:squad-members state) %)) (my-units)))]
        (dosync
         (commute strategy-state assoc-in [:squad-orders squad :type] (get-unit-type-kw leader)))))))

(defn assign-squad-targets []
  (let [state @strategy-state]
    (doseq [squad (keys (map-invert (:squad-members state)))]
      (let [members (map first (filter #(= (second %) squad) (:squad-members state)))
            leader (first members)
            attackable (filter #(can-attack? leader %) (enemy-units))
            votes (remove nil? (map #(closest % attackable) members))]
        (when (seq votes)
          (let [target (first (apply max-key second (frequencies votes)))]
            (dosync
             (commute strategy-state assoc-in [:squad-orders squad :target] target))))))))

(defn dispatch-on-squad-type [squad members]
  (or (get-in @strategy-state [:squad-orders squad :type]) :default))
(defmulti assign-special-orders dispatch-on-squad-type)

(defmethod assign-special-orders :ghost [squad members]
  (let [state @strategy-state
        previous-orders (-> (get-in state [:squad-orders squad :lockdown])
                            (select-keys members)
                            (map-invert))
        assigned-ghosts (set (keys (map-invert previous-orders)))
        lockdown-capable (filter #(>= (energy %) 100) members)
        mech? (fn [x] (or (mechanical? x) (robotic? x)))
        mechs (filter mech? (enemy-units))
        nearby-mechs (->> (map #(units-nearby % 300 mechs) members)
                          (set)
                          (apply union))
        targets (filter (complement locked-down?*) nearby-mechs)
        available (difference (set lockdown-capable) assigned-ghosts)
        cleaned-orders (select-keys previous-orders targets)
        new-targets (difference (set targets) (set (keys cleaned-orders)))]
    (loop [orders cleaned-orders
           available available
           targets new-targets]
      (if (or (not (seq targets)) (not (seq available)))
        (dosync
         (commute strategy-state assoc-in [:squad-orders squad :lockdown] (map-invert orders)))
        (recur (assoc orders (first targets) (first available))
               (next available)
               (next targets))))))

(defmethod assign-special-orders :default [squad members])

(defn run-strategy-engine []
  (let [enemy-base (first (enemy-start-locations))
        combat-units (filter (every-pred completed? combat-unit?) (my-units))]
    (reform-squads)
    (tag-squads)
    (assign-squad-targets)
    (doseq [squad (keys (map-invert (:squad-members @strategy-state)))]
      (let [members (->> (seq (:squad-members @strategy-state))
                         (filter #(= (second %) squad))
                         (map first))]
        (assign-special-orders squad members)))))

(defn start-strategy-engine! []
  (dosync
   (commute strategy-state assoc-in [:frame] 0)
   (commute strategy-state assoc-in [:run] true)
   (commute strategy-state assoc-in [:enemy-units] {})
   (commute strategy-state assoc-in [:nukes] {})
   (commute strategy-state assoc-in [:squad-members] {})
   (commute strategy-state assoc-in [:squad-orders] {}))
  (future (loop []
            (if (not (:run @strategy-state))
              nil
              (let [frame (frame-count)]
                (if (and (> frame (:frame @strategy-state))
                         (not @repl-control))
                  (do (try
                        (run-strategy-engine)
                      (catch Exception e
                        (println "Strategy engine exception!")
                        (.printStackTrace e)))
                      (dosync
                       (commute strategy-state assoc-in [:frame] frame)))
                  (Thread/sleep 1))
                (recur))))))

(defn stop-strategy-engine! []
  (dosync
   (commute strategy-state assoc-in [:run] false)))
