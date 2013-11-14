;; In Brood War, when a build command is issued to a unit, this happens in sequence:
;; Unit moves to location -> Check for resources -> Building cost is deducted -> Building is placed

;; Contracts provide a way to keep track of what buildings you've committed to build
;; before your worker actually gets to the location. This allows you to make
;; reasonable decisions based on the mineral and gas counts that you have not
;; already allocated to future buildings.

(ns korhal.tools.contract
  (:refer-clojure :exclude [load])
  (:require [clojure.set :refer [intersection]]
            [korhal.interop.interop :refer :all])
  (:import (jnibwapi.model Unit)))

(declare building-tiles reserved-tiles)

(def contract-display (atom false))
(def current-contract-id (atom 0))
(def contracted (ref {:minerals 0 :gas 0 :supply 0 :buildings [] :building-ids {}}))

(defn clear-contract-atoms []
  (dosync (ref-set contracted {:minerals 0 :gas 0 :supply 0 :buildings [] :building-ids {}})))

(defn show-contract-display [bool] (reset! contract-display bool))

(defn- get-contract-id []
  (let [curr-val @current-contract-id]
    (swap! current-contract-id inc)
    curr-val))

(defn available-minerals [] (max 0 (- (my-minerals) (:minerals @contracted))))

(defn available-gas [] (max 0 (- (my-gas) (:gas @contracted))))

(defn contracted-max-supply
  "Includes current max supply, contracted depots, and unfinished depots."
  []
  (let [unfinished-depot? (every-pred (complement completed?) is-supply-depot?)]
    (+ (my-supply-total) (:supply @contracted) (* 8 (count (filter unfinished-depot? (my-buildings)))))))

(defn- draw-contract-display []
  (draw-text 380 20 "Contracted" true)
  (draw-text 450 20 (:minerals @contracted) true)
  (draw-text 520 20 (:gas @contracted) true)
  (draw-text 590 20 (:supply @contracted) true)
  (draw-text 380 35 "Available" true)
  (draw-text 450 35 (available-minerals) true)
  (draw-text 520 35 (available-gas) true)
  (draw-text 590 35 (contracted-max-supply) true))

(defn- contract-building
  "Add a new building to the ref of contracted buildings."
  [builder build-type tiles]
  (dosync
   (commute contracted update-in [:minerals] + (mineral-price build-type))
   (commute contracted update-in [:gas] + (gas-price build-type))
   (commute contracted update-in [:supply] + (supply-provided build-type))
   (commute contracted update-in [:buildings] conj {:id (get-contract-id)
                                                    :builder builder
                                                    :type build-type
                                                    :tiles tiles})))

(defn- decontract-building
  "Remove the first building matching this builder and build-type from
  the contract ref if such a building exists. IMPORTANT: This system
  will fall apart if you start queuing multiple buildings at a single
  time for a worker to build. This assumes a worker can only contract
  one building at a time, which you really should be doing anyway."
  [builder build-type]
  (let [is-decontract-map? (fn [v] (and (= (:builder v) builder)
                                        (= (:type v) build-type)))
        to-cancel (first (filter is-decontract-map? (:buildings @contracted)))]
    (when to-cancel
      (dosync
       (let [matches-id? (fn [v] (= (:id v) (:id to-cancel)))]
         (commute contracted update-in [:minerals] - (mineral-price build-type))
         (commute contracted update-in [:gas] - (gas-price build-type))
         (commute contracted update-in [:supply] - (supply-provided build-type))
         (commute contracted update-in [:buildings] #(remove matches-id? %)))))))

(defn contract-build
  "Replaces the build function from the standard API. You should check
  to make sure the building placement is valid before calling this
  function."
  ([builder point to-build] (contract-build builder (.x point) (.y point) to-build))
  ([builder tx ty to-build]
     (let [build-type (get-type (to-build unit-type-kws))]
       (contract-building builder build-type (building-tiles tx ty build-type))
       (build builder tx ty to-build))))

(defn cancel-contracts [unit-or-unit-id]
  "Cancel all contracts associated with a given unit."
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (doseq [building (filter #(= unit-id (get-id (:builder %))) (:buildings @contracted))]
      (decontract-building (:builder building) (:type building)))))

(defn- clear-on-new-buildings []
  "When a building is placed, we will be able to see it in the list of
  my buildings. Since it has been placed, the cost has been deducted
  from our available resources, so we no longer need the contract. We
  clear out any contracts for buildings that already exist on the map."
  (doseq [new-building (filter #((complement contains?) (:building-ids @contracted) (get-id %)) (my-buildings))]
    ;; special case, do not decontract the CC you start with
    (when-not (<= (frame-count) 1)
      (decontract-building (get-unit-by-id (build-unit-id new-building))
                           (get-type new-building)))
    (dosync
     (commute contracted update-in [:building-ids] merge {(get-id new-building) new-building}))))

(defn clear-contracts []
  (when @contract-display (draw-contract-display))
  (clear-on-new-buildings))

(defn- building-tiles
  "Given a start tile and a building type, return a vector of all
  tiles the building will be placed on."
  [tx ty build-type]
  (for [tx (range tx (+ tx (tile-width build-type)))
        ty (range ty (+ ty (tile-height build-type)))]
    [tx ty]))

(defn- reserved-tiles
  "Return a set of all tiles reserved by currently contracted buildings."
  []
  (set (apply concat (map :tiles (:buildings @contracted)))))

(defn can-build?
  "Checks whether a given building type fits in a specified
  location. Also takes into account buildings that are contracted to
  be built but do not yet exist on the map."
  [tx ty to-build check-explored]
  (let [build-type (to-build unit-type-kws)
        tiles (building-tiles tx ty build-type)]
    (when (not (seq (intersection (set tiles) (reserved-tiles))))
      (every? #(can-build-here? (first %) (second %) build-type check-explored) tiles))))