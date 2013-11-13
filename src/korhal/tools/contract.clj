(ns korhal.tools.contract
  (:require [clojure.set :refer [union]]
            [korhal.interop.interop :refer [build my-minerals my-gas my-supply-total
                                            unit-type-kws mineral-price gas-price
                                            supply-provided my-buildings my-units
                                            my-scvs get-unit-by-id
                                            completed? is-supply-depot? get-id
                                            build-unit-id get-type frame-count
                                            draw-text]]))

(def contract-display (atom false))
(def contracted (ref {:minerals 0 :gas 0 :supply 0 :buildings [] :building-ids {}}))

(defn clear-contract-atoms []
  (dosync (ref-set contracted {:minerals 0 :gas 0 :supply 0 :buildings [] :building-ids {}})))

(defn show-contract-display [bool] (reset! contract-display bool))

(defn- draw-contract-display []
  (draw-text 380 20 "Contracted" true)
  (draw-text 450 20 (:minerals @contracted) true)
  (draw-text 520 20 (:gas @contracted) true)
  (draw-text 590 20 (:supply @contracted) true))

(defn available-minerals [] (max 0 (- (my-minerals) (:minerals @contracted))))

(defn available-gas [] (max 0 (- (my-gas) (:gas @contracted))))

(defn contracted-max-supply []
  (let [unfinished-depot? (every-pred (complement completed?) is-supply-depot?)]
    (+ (my-supply-total) (:supply @contracted) (* 8 (count (filter unfinished-depot? (my-buildings)))))))

(defn- contract-building [builder build-type]
  (println (str "new contract from worker " (get-id builder)))
  (dosync
   (commute contracted update-in [:minerals] + (mineral-price build-type))
   (commute contracted update-in [:gas] + (gas-price build-type))
   (commute contracted update-in [:supply] + (supply-provided build-type))
   (commute contracted update-in [:buildings] conj {:builder builder :type build-type})))

(defn- decontract-building [builder build-type]
  (let [is-decontract-map? (fn [v] (= v {:builder builder :type build-type}))]
    (println (str "removing contract from worker " (get-id builder)))
    (dosync
     (commute contracted update-in [:minerals] - (mineral-price build-type))
     (commute contracted update-in [:gas] - (gas-price build-type))
     (commute contracted update-in [:supply] - (supply-provided build-type))
     (commute contracted update-in [:buildings] #(remove is-decontract-map? %)))))

(defn contract-build
  ([builder point to-build] (contract-build builder (.x point) (.y point) to-build))
  ([builder tile-x tile-y to-build]
     (let [build-type (to-build unit-type-kws)]
       (contract-building builder build-type)
       (build builder tile-x tile-y to-build))))

(defn contract-worker-died [unit-id]
  (doseq [building (filter #(= unit-id (get-id (:builder %))) (:buildings @contracted))]
    (decontract-building (:builder building) (:type building))))

(defn cancel-contracts [unit]
  (contract-worker-died (get-id unit)))

(defn clear-contracts []
  (when @contract-display (draw-contract-display))
  (doseq [new-building (filter #((complement contains?) (:building-ids @contracted) (get-id %)) (my-buildings))]
    ;; special case, do not decontract the CC you start with
    (when-not (<= (frame-count) 1)
      (decontract-building (get-unit-by-id (build-unit-id new-building))
                           (get-type new-building)))
    (dosync
     (commute contracted update-in [:building-ids] merge {(get-id new-building) new-building}))))
