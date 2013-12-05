(ns korhal.micro.role
  (:require [korhal.interop.interop :refer :all]
            [korhal.micro.state :refer [micro-state micro-inform!]]
            [korhal.tools.queue :refer [with-api with-api-when]]))

(defn micro-mineral-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-gas? unit)))
    (let [closest-cc (closest-tile unit (my-command-centers))
          cc-minerals (units-nearby closest-cc 600 (minerals))
          freq-seed (into {} (map #(vector % 0) (map get-id cc-minerals)))
          mining-freq (->> (seq (:mining @micro-state))
                           (map second)
                           (map :mineral)
                           (frequencies)
                           (merge freq-seed))
          cc-minerals-freq (select-keys mining-freq (map get-id cc-minerals))
          sorted (sort-by second cc-minerals-freq)]
      (when-let [mineral (if (seq sorted)
                           (get-unit-by-id (first (first sorted)))
                           (first cc-minerals))]
        (micro-inform! :mining {:id (get-id unit)
                                :mineral (get-id mineral)})
        (with-api (right-click unit mineral))))))

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
