(ns korhal.micro.role
  (:require [korhal.interop.interop :refer :all]
            [korhal.tools.queue :refer [with-api with-api-when]]))

(defn micro-mineral-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-gas? unit)))
    (when-let [closest-mineral (closest unit (minerals))]
      (with-api (right-click unit closest-mineral)))))

(defn micro-gas-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-minerals? unit)))
    (when-let [closest-refinery (closest unit (my-refineries))]
      (when (and closest-refinery (completed? closest-refinery))
        (with-api (right-click unit closest-refinery))))))

(defn micro-early-scout [unit]
  (let [enemy-base (first (enemy-start-locations))]
    (with-api (move unit (pixel-x enemy-base) (pixel-y enemy-base)))))

(defn micro-defender [unit base-choke]
  (when (and (completed? unit)
             (idle? unit))
    (if (> (dist-choke unit base-choke) 300)
      (with-api (attack unit (center-x base-choke) (center-y base-choke)))
      (when (and (is-siege-tank-tank-mode? unit) (researched? :tank-siege-mode))
        (siege unit)))))

(defn micro-attacker [unit attack-location]
  (when (and attack-location ((every-pred completed? idle?) unit))
    (with-api (attack unit (pixel-x attack-location) (pixel-y attack-location)))))

(defn micro-under-aoe [unit storms]
  (let [storm-range 288]
    (when (and (under-storm? unit) (not (moving? unit)))
      (with-api (move unit (- (pixel-x unit) 100) (- (pixel-y unit) 100))))))
