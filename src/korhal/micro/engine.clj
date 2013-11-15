(ns korhal.micro.engine
  (:refer-clojure :exclude [load])
  (:require [korhal.interop.interop :refer :all])
  (:import (jnibwapi.model Unit)))

(def micro-state (ref {:tags {}}))

(defn start-micro-engine []
  (dosync
   (commute micro-state assoc-in [:tags] {})))

(defn micro-tag-unit! [unit-or-unit-id tag]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (dosync
     (commute micro-state assoc-in [:tags unit-id] tag))))

(defn get-micro-tag [unit-or-unit-id]
  (let [unit-id (if (instance? Unit unit-or-unit-id) (get-id unit-or-unit-id) unit-or-unit-id)]
    (get-in @micro-state [:tags unit-id])))

(defn- micro-early-scout [unit]
  (let [enemy-base (first (enemy-start-locations))]
    (move unit (pixel-x enemy-base) (pixel-y enemy-base))))

(defn run-micro-engine []
  (doseq [unit (filter (complement building?) (my-units))]
    (condp = (:role (get-micro-tag unit))
      nil nil
      :mineral (when (idle? unit)
                  (let [closest-mineral (apply min-key (partial dist unit) (minerals))]
                    (right-click unit closest-mineral)))
      :early-scout (micro-early-scout unit)
      :else nil)))
