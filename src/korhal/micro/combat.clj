(ns korhal.micro.combat
  (:require [korhal.interop.interop :refer :all]
            [korhal.micro.state :refer [micro-state micro-inform!]]
            [korhal.strategy.query :refer [get-squad-orders]]
            [korhal.tools.queue :refer [with-api with-api-unit
                                        clear-api-unit-tag api-unit-tag]]))

(defn- micro-combat-attack [unit]
  (when (idle? unit)
    (clear-api-unit-tag unit)
    (let [squad-target (:target (get-squad-orders unit))
          enemy (if squad-target
                  squad-target
                  (closest unit (units-nearby unit 1000 (enemy-units))))
          px (when enemy (pixel-x enemy))
          py (when enemy (pixel-y enemy))]
      (with-api
        (when (idle? unit)
          (if (and enemy (visible? enemy))
            (attack unit enemy)
            (attack unit px py)))))))

(defn- micro-combat-stim [unit]
  (if (and (or (is-marine? unit) (is-firebat? unit))
           (>= (health-perc unit) 0.5)
           (researched? :stim-packs)
           (not (stimmed? unit)))
    (with-api
      (when-not (stimmed? unit)
        (use-tech unit (tech-type-kws :stim-packs))))))

(defn- close-melee? [unit enemy]
  (and (ground-melee? enemy)
       (< (dist unit enemy) (- (max-range (ground-weapon unit)) 2))))

(defn- repulsion-angle
  "Bisect the biggest available escape sector."
  [unit coll]
  (let [angles-to (sort (map (partial angle-to unit) coll))]
    (cond
     (zero? (count angles-to)) nil
     (= 1 (count angles-to)) (+ (first angles-to) 180 45) ;; curve around
     :else (let [pairs (for [idx (range (dec (count angles-to)))
                             :let [a (nth angles-to idx)
                                   b (nth angles-to (inc idx))]]
                         [a b])
                 pairs-with-last (conj pairs [(last angles-to) (+ 360 (first angles-to))])
                 best (apply max-key #(- (second %) (first %)) pairs-with-last)
                 diff (- (second best) (first best))
                 bisected (+ (first best) (/ diff 2))]
             (if (> diff 270)
               (+ 45 bisected) ;; curve around so we don't just run away in a straight line forever
               bisected)))))

(defn- micro-combat-kite [unit]
  (when-not (= :kite (api-unit-tag unit))
    (with-api-unit unit :kite 5
      (let [enemy-melee (filter (partial close-melee? unit) (enemy-units))
            closest-enemy (closest unit enemy-melee)
            squad-target (:target (get-squad-orders unit))
            target-enemy (if squad-target squad-target closest-enemy)
            kite-angle (repulsion-angle unit enemy-melee)
            kite-dist 50
            fire-range (- (max-range (ground-weapon unit)) 15)]
        (when kite-angle
          (cond
           (and (not (attack-frame? unit))
                (zero? (ground-weapon-cooldown unit))
                (> (dist unit closest-enemy) fire-range)) (attack unit target-enemy)
           (<= (dist unit closest-enemy) fire-range) (move-angle unit kite-angle kite-dist)))))))

(defn- micro-combat-heal [unit]
  (let [injured? (fn [target] (not= (health-perc target) 1))
        organics (filter organic? (my-units))
        nearby-injured (filter injured? (units-nearby unit 128 organics))]
    (if (seq nearby-injured)
      (with-api (attack unit (apply min-key health-perc nearby-injured)))
      (let [outer-injured (filter injured? (units-nearby unit 1000 organics))]
        (when (seq outer-injured)
          (with-api (attack unit (apply min-key health-perc outer-injured))))))))

(defn- micro-combat-cower [unit]
  (when-not (= :cower (api-unit-tag unit))
    (with-api-unit unit :cower 5
      (let [threats (units-nearby unit 256 (remove building? (enemy-units)))
            in-range-of (attackable-by unit threats 64)
            cower-angle (repulsion-angle unit threats)
            cower-dist 50]
        (when (and cower-angle (seq in-range-of))
          (move-angle unit cower-angle cower-dist))))))

(defn locked-down?* [unit]
  (or (not (zero? (lockdown-timer unit)))
      (get-in @micro-state [:lockdown (get-id unit)])))

(defn- micro-combat-lockdown [unit]
  (when (and (researched? :lockdown)
             (>= (energy unit) 100))
    (when-let [target (get-in (get-squad-orders unit) [:lockdown unit])]
      (with-api
        (when (and (>= (energy unit) 100)
                   (not (locked-down?* target))
                   ;; hard to tell when command was issued to fire lockdown
                   ;; using attack-frame doesn't work, so we use last-command-frame
                   (>= (- (frame-count) (last-command-frame unit)) 20))
          (when (use-tech unit (tech-type-kws :lockdown) target)
            (micro-inform! :lockdown {:id (get-id target)
                                      :frame (frame-count)})))))))

(defn dispatch-on-unit-type-kw [unit] (or (get-unit-type-kw unit) :default))
(defmulti micro-combat dispatch-on-unit-type-kw)

(defmethod micro-combat :scv [unit]
  (if (<= (health-perc unit) 0.25)
    (micro-combat-cower unit)
    (micro-combat-attack unit)))

(defmethod micro-combat :marine [unit]
  (micro-combat-stim unit)
  (micro-combat-kite unit)
  (micro-combat-attack unit))

(defmethod micro-combat :firebat [unit]
  (micro-combat-stim unit)
  (micro-combat-attack unit))

(defmethod micro-combat :vulture [unit]
  (micro-combat-kite unit)
  (micro-combat-attack unit))

(defmethod micro-combat :medic [unit]
  (micro-combat-cower unit)
  (micro-combat-heal unit))

(defmethod micro-combat :ghost [unit]
  (let [orders (get-squad-orders unit)
        lockdown-target (get-in orders [:lockdown unit])]
    (if (and lockdown-target (not (locked-down?* lockdown-target)))
      (micro-combat-lockdown unit)
      (micro-combat-attack unit))))

(defmethod micro-combat :siege-tank-tank-mode [unit])

(defmethod micro-combat :siege-tank-siege-mode [unit])

(defmethod micro-combat :goliath [unit])

(defmethod micro-combat :wraith [unit])

(defmethod micro-combat :science-vessel [unit])

(defmethod micro-combat :dropship [unit])

(defmethod micro-combat :battlecruiser [unit])

(defmethod micro-combat :valkyrie [unit])

(defmethod micro-combat :missile-turret [unit]
  (micro-combat-attack unit))

(defmethod micro-combat :default [unit]
  (micro-combat-attack unit))
