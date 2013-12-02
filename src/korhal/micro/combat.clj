(ns korhal.micro.combat
  (:require [korhal.interop.interop :refer :all]
            [korhal.tools.queue :refer [with-api with-api-when]]))

(defn- micro-combat-attack [unit]
  (when (and (idle? unit) (not (enemies-in-range unit)))
    (let [enemy (closest unit (enemies-nearby unit 1000))
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
           (researched? :stim-packs))
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
     (= 1 (count angles-to)) (+ (first angles-to) 180)
     :else (let [pairs (for [idx (range (dec (count angles-to)))
                             :let [a (nth angles-to idx)
                                   b (nth angles-to (inc idx))]]
                         [a b])
                 best (apply max-key #(- (second %) (first %)) pairs)]
             (+ 180 (first best) (/ (- (second best) (first best)) 2))))))

(defn- micro-combat-kite [unit]
  (let [enemy-melee (filter (partial close-melee? unit) (enemy-units))
        closest-enemy (closest unit enemy-melee)
        kite-angle (repulsion-angle unit enemy-melee)]
    (when kite-angle
      (with-api-when
        (or (not (zero? (ground-weapon-cooldown unit)))
            (= unit (target-unit closest-enemy)))
        (move-angle unit kite-angle 100)))))

(defn dispatch-on-unit-type-kw [unit] (get-unit-type-kw unit))
(defmulti micro-combat dispatch-on-unit-type-kw)

(defmethod micro-combat :marine [unit]
  (micro-combat-stim unit)
  (micro-combat-kite unit)
  (micro-combat-attack unit))

(defmethod micro-combat :default [unit]
  (micro-combat-attack unit))
