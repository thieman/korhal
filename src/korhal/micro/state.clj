(ns korhal.micro.state
  (:require [korhal.interop.interop :refer :all])
  (:import (jnibwapi.model Unit)))

(def micro-state (ref {:tags {} :frame 0 :lockdown {} :mining {}}))

(defn micro-inform! [tag-type doc]
  (dosync
   (if (:id doc)
     (commute micro-state assoc-in [tag-type (:id doc)] doc)
     (commute micro-state update-in [tag-type] conj doc))))

(defn micro-remove! [tag-type id]
  (dosync
   (commute micro-state update-in [tag-type] #(dissoc % id))))

(defn micro-expire! [tag-type frame-diff]
  (dosync
   (let [current (@micro-state tag-type)
         keep (for [[k v] current
                    :when (not (>= (- (frame-count) (:frame v)) frame-diff))]
                k)]
     (commute micro-state update-in [tag-type] select-keys keep))))

(defn micro-tag-unit! [unit-or-unit-id tag]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (dosync
     (commute micro-state assoc-in [:tags unit-id] tag))))

(defn get-micro-tag [unit-or-unit-id]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (get-in @micro-state [:tags unit-id])))

(defn micro-tag-new-unit! [unit]
  (let [unit-type (get-unit-type unit)]
    (condp = unit-type
      (get-unit-type (:scv unit-type-kws)) nil
      (micro-tag-unit! unit {:role :defend}))))
