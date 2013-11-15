(ns korhal.macro.state
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all]
            [korhal.macro.build-order :refer [build-orders]])
  (:import (jnibwapi.model Unit)))

(def macro-state (ref {:build-order [] :tags {}}))

(defn start-macro-engine []
  (dosync
   (commute macro-state assoc-in [:build-order] (build-orders :test-order))
   (commute macro-state assoc-in [:tags] {})))

(defn macro-tag-unit! [unit-or-unit-id tag]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (dosync
     (commute macro-state assoc-in [:tags unit-id] tag))))

(defn get-macro-tag [unit-or-unit-id]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (get-in @macro-state [:tags unit-id])))

(defn pop-build-order! []
  (dosync
   (commute macro-state update-in [:build-order] nnext)))

(defn builder-to-constructor!
  "When a new building is placed, updated the building SCV's macro tag
  to show that it successfully placed the buliding and is now
  constructing it."
  [building]
  (let [builder (get-unit-by-id (build-unit-id building))]
    (macro-tag-unit! builder {:role :construct :building building})))
