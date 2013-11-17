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

(defn- micro-mineral-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-gas? unit)))
    (let [closest-mineral (apply min-key (partial dist unit) (minerals))]
      (right-click unit closest-mineral))))

(defn- micro-gas-worker [unit]
  (when (and (completed? unit) (or (idle? unit) (gathering-minerals? unit)))
    (let [closest-refinery (apply min-key (partial dist unit) (my-refineries))]
      (when (and closest-refinery (completed? closest-refinery))
        (right-click unit closest-refinery)))))

(defn- micro-early-scout [unit]
  (let [enemy-base (first (enemy-start-locations))]
    (move unit (pixel-x enemy-base) (pixel-y enemy-base))))

(defn- micro-defender [unit base-choke]
  (let [unit-type (get-unit-type unit)]
    (when ((complement completed?) unit)
      (right-click unit (center-x base-choke) (center-y base-choke)))))

(defn micro-tag-new-unit! [unit]
  (let [unit-type (get-unit-type unit)]
    (condp = unit-type
      (get-unit-type (:scv unit-type-kws)) nil
      (micro-tag-unit! unit {:role :defend}))))

(defn run-micro-engine []
  (let [base-choke (apply min-key (partial dist-choke (first (my-command-centers))) (chokepoints))]
    (doseq [unit (filter (complement building?) (my-units))]
      (condp = (:role (get-micro-tag unit))
        nil nil
        :mineral (micro-mineral-worker unit)
        :gas (micro-gas-worker unit)
        :early-scout (micro-early-scout unit)
        :defend (micro-defender unit base-choke)
        :else nil))))
