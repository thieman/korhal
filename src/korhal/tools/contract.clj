;; In Brood War, when a build command is issued to a unit, this happens in sequence:
;; Unit moves to location -> Check for resources -> Building cost is deducted -> Building is placed

;; Contracts provide a way to keep track of what buildings you've committed to build
;; before your worker actually gets to the location. This allows you to make
;; reasonable decisions based on the mineral and gas counts that you have not
;; already allocated to future buildings.

;; Contracts also keep track of your spending within each frame of the game update loop. This
;; is important since the game's value of your available resources is only updated at the
;; beginning of each loop.

(ns korhal.tools.contract
  (:require [clojure.set :refer [intersection]]
            [korhal.interop.interop :refer :all]
            [korhal.tools.queue :refer [with-api]])
  (:import (jnibwapi.model Unit)))

(declare building-tiles reserved-tiles)

(def contract-display (atom false))
(def current-contract-id (atom 0))
(def contracted (ref {:minerals 0 :gas 0 :supply 0 :buildings [] :building-ids {}
                      :minerals-this-frame 0 :gas-this-frame 0}))

(defn clear-contract-atoms []
  (dosync (ref-set contracted {:minerals 0 :gas 0 :supply 0 :buildings [] :building-ids {}
                               :minerals-this-frame 0 :gas-this-frame 0})))

(defn show-contract-display [bool] (reset! contract-display bool))

(defn- get-contract-id []
  (let [curr-val @current-contract-id]
    (swap! current-contract-id inc)
    curr-val))

(defn available-minerals [] (max 0 (- (my-minerals) (:minerals @contracted) (:minerals-this-frame @contracted))))

(defn available-gas [] (max 0 (- (my-gas) (:gas @contracted) (:gas-this-frame @contracted))))

(defn contracted-max-supply
  "Includes current max supply, contracted depots, and unfinished depots."
  []
  (let [unfinished-depot? (every-pred (complement completed?) is-supply-depot?)]
    (+ (my-supply-total) (:supply @contracted) (* 8 (count (filter unfinished-depot? (my-buildings)))))))

(defn contracted-addons [building]
  (filter #(= (:builder %) building) (:buildings @contracted)))

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
  [builder build-kw build-type tiles]
  (dosync
   (commute contracted update-in [:minerals] + (mineral-price build-type))
   (commute contracted update-in [:gas] + (gas-price build-type))
   (commute contracted update-in [:supply] + (supply-provided build-type))
   (commute contracted update-in [:buildings] conj {:id (get-contract-id)
                                                    :kw build-kw
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
  ([builder tx ty to-build] (contract-build builder tx ty to-build true))
  ([builder tx ty to-build accommodate-addon]
     (let [build-type (get-unit-type (to-build unit-type-kws))]
       (contract-building builder to-build build-type (building-tiles tx ty build-type accommodate-addon))
       (if (tile-explored? tx ty)
         (with-api (build builder tx ty to-build))
         (with-api (move builder (* 32 tx) (* 32 ty)))))))

(defn add-unit-costs-to-frame [unit]
  (dosync
   (commute contracted update-in [:minerals-this-frame] + (mineral-price unit))
   (commute contracted update-in [:gas-this-frame] + (gas-price unit))))

(defn contract-build-addon
  "Replaces the build-addon function from the standard API."
  [building to-build]
  (let [build-type (get-unit-type (to-build unit-type-kws))]
    (contract-building building to-build build-type (building-tiles building))
    (with-api (build-addon building to-build))))

(defn contract-train
  "Replaces the train function from the standard API."
  [building to-train]
  (add-unit-costs-to-frame (get-unit-type (to-train unit-type-kws)))
  (with-api (train building to-train)))

(defn contract-morph
  "Replaces the morph function from the standard API."
  [unit morph-to]
  (add-unit-costs-to-frame (get-unit-type (morph-to unit-type-kws)))
  (with-api (morph unit morph-to)))

(defn contract-research
  "Replaces the research function from the standard API."
  [unit to-research]
  (add-unit-costs-to-frame (get-tech-type (to-research tech-type-kws)))
  (with-api (research unit to-research)))

(defn contract-upgrade
  "Replaces the upgrade function from the standard API."
  [unit to-upgrade]
  (add-unit-costs-to-frame (get-upgrade-type (to-upgrade upgrade-type-kws)))
  (with-api (upgrade unit to-upgrade)))

(defn cancel-contracts [unit-or-unit-id]
  "Cancel all contracts associated with a given unit."
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (doseq [building (filter #(= unit-id (get-id (:builder %))) (:buildings @contracted))]
      (decontract-building (:builder building) (:type building)))))

(defn- clear-frame-resources []
  (dosync
   (commute contracted assoc-in [:minerals-this-frame] 0)
   (commute contracted assoc-in [:gas-this-frame] 0)))

(defn contract-add-initial-cc []
  (let [cc (first (my-command-centers))]
    (dosync
     (commute contracted update-in [:building-ids] merge {(get-id cc) cc}))))

(defn contract-add-new-building [new-building]
  (decontract-building (get-unit-by-id (build-unit-id new-building))
                       (get-unit-type new-building))
  (dosync
   (commute contracted update-in [:building-ids] merge {(get-id new-building) new-building})))

(defn clear-contracts []
  (when @contract-display (draw-contract-display))
  (clear-frame-resources))

(defn- building-tiles
  "Given a start tile and a building type, return a vector of all
  tiles the building will be placed on."
  ([building]
     (let [start-x (+ (tile-x building) (tile-width building))
           start-y (+ (tile-y building) (tile-height building) -2)]
       (for [x (range start-x (+ start-x 2))
             y (range start-y (+ start-y 2))]
         [x y])))
  ([tx ty build-type] (building-tiles tx ty build-type true))
  ([tx ty build-type accommodate-addon]
     (let [base-tiles (for [tx (range tx (+ tx (tile-width build-type)))
                            ty (range ty (+ ty (tile-height build-type)))]
                        [tx ty])]
       (if (and accommodate-addon (supports-addon? build-type))
         (let [max-x (apply max (map first base-tiles))
               max-y (apply max (map second base-tiles))
               addon-tiles (for [tx (range (+ 1 max-x) (+ 3 max-x))
                                 ty (range (- max-y 1) (+ max-y 1))]
                             [tx ty])]
           (concat base-tiles addon-tiles))
         base-tiles))))

(defn- reserved-tiles
  "Return a set of all tiles reserved by currently contracted buildings."
  []
  (set (apply concat (map :tiles (:buildings @contracted)))))

(defn can-build?
  "Checks whether a given building type fits in a specified
  location. Also takes into account buildings that are contracted to
  be built but do not yet exist on the map."
  ([tx ty to-build check-explored] (can-build? tx ty to-build check-explored true))
  ([tx ty to-build check-explored accommodate-addon]
     (let [build-type (to-build unit-type-kws)
           tiles (building-tiles tx ty build-type accommodate-addon)]
       (when (not (seq (intersection (set tiles) (reserved-tiles))))
         (every? #(can-build-here? (first %) (second %) build-type check-explored) tiles)))))

(defn can-afford?
  "Check whether there are enough resources to build a particular
  unit, upgrade, or tech kw. Includes supply."
  [to-buy]
  (let [unit-type (if-not (keyword? to-buy)
                    to-buy
                    (cond
                     (contains? unit-type-kws to-buy) (get-unit-type (to-buy unit-type-kws))
                     (contains? upgrade-type-kws to-buy) (get-upgrade-type (to-buy upgrade-type-kws))
                     (contains? tech-type-kws to-buy) (get-tech-type (to-buy tech-type-kws))))]
    (and (>= (available-minerals) (mineral-price unit-type))
         (>= (available-gas) (gas-price unit-type))
         (>= (my-supply-total) (+ (my-supply-used) (supply-required unit-type))))))
