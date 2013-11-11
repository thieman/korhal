(ns korhal.interop
  (:require [korhal.interop-types :refer [unit-types unit-type-fn-maps unit-fn-maps]])
  (:import (jnibwapi.model Map Player Unit BaseLocation Region ChokePoint)
           (jnibwapi.types.UnitType$UnitTypes)))

(declare get-type)

(def ^:dynamic api nil)
(defn bind-api [binding] (alter-var-root (var api) #(identity %2) binding))

;; type definitions

(defn gen-type-ids []
  (intern *ns*
          (symbol 'type-ids)
          (->> (map #(vector (eval `(.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                             (eval (symbol (str "jnibwapi.types.UnitType$UnitTypes/" %))))
                   (take-nth 2 (rest unit-types)))
               (flatten)
               (apply hash-map))))
(gen-type-ids)

(defn gen-type-kw-lookup []
  (intern *ns*
          (symbol 'type-kw-lookup)
          (merge {:mineral 'Resource_Mineral_Field :geyser 'Resource_Vespene_Geyser}
                 (zipmap (map keyword (take-nth 2 unit-types)) (take-nth 2 (rest unit-types))))))

(gen-type-kw-lookup)

;; common calls to get state vars and collections

(defn my-minerals [] (.. api getSelf getMinerals))

(defn my-gas [] (.. api getSelf getGas))

(defn my-supply-used [] (.. api getSelf getSupplyUsed))

(defn my-supply-total [] (.. api getSelf getSupplyTotal))

(defn my-units [] (.getMyUnits api))

(defn enemy-units [] (.getEnemyUnits api))

(defn neutral-units [] (.getNeutralUnits api))

(defn minerals []
  (filter #(= (.getTypeID %) (.getID jnibwapi.types.UnitType$UnitTypes/Resource_Mineral_Field))
          (.getNeutralUnits api)))

(defn geysers []
  (filter #(= (.getTypeID %) (.getID jnibwapi.types.UnitType$UnitTypes/Resource_Vespene_Geyser))
          (.getNeutralUnits api)))

;; generate single unit functions

(defmacro define-unit-type-fns []
  (let [dynamic-dot-form (fn [instance method] `(. ~instance ~method))]
    (cons `do
          (for [[clj-name java-name] (partition 2 unit-type-fn-maps)]
            `(defn ~clj-name [unit#] (. (get-type unit#) ~java-name))))))

(define-unit-type-fns)

(defmacro define-unit-fns []
  (let [dynamic-dot-form (fn [instance method] `(. ~instance ~method))]
    (cons `do
          (for [[clj-name java-name] (partition 2 unit-fn-maps)]
            `(defn ~clj-name [unit#] (. unit# ~java-name))))))

(define-unit-fns)

;; common API commands shared among multiple types

(defn get-id [obj] (.getID obj))

(defn get-type [unit] (.getUnitType api (.getTypeID unit)))

(defn right-click [selected target]
  (.rightClick api (.getID selected) (.getID target)))

;; unit commands

(defn attack [unit target]
  (.attack api (.getID unit) (.getX target) (.getY target)))

(defn build [builder tile-x tile-y to-build]
  (.build api (.getID builder) tile-x tile-y
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(to-build type-kw-lookup))))))

(defn morph [unit morph-to]
  (.morph api
          (.getID unit)
          (.getID (eval `(. jnibwapi.types.UnitType$UnitTypes ~(morph-to type-kw-lookup))))))

;; type predicates, e.g. is-drone?
(doseq [[n t] (partition 2 unit-types)]
  (let [class-type (eval `(.getID ~(symbol (str "jnibwapi.types.UnitType$UnitTypes/" t))))]
    (intern *ns*
            (symbol (str "is-" n "?"))
            (fn [unit] (= (.getTypeID unit) class-type)))))

;; own unit type collections, e.g. my-drones
(doseq [[n _] (partition 2 unit-types)]
  (let [type-predicate (eval (symbol (str *ns* "/is-" n "?")))]
    (intern *ns*
            (symbol (str "my-" n "s"))
            (fn [] (filter type-predicate (.getMyUnits api))))))

;; utility functions

(defn swap-key [curr-val k v]
  (merge curr-val {k v}))

(defn swap-keys [swap-atom & forms]
  (doseq [[k v] (partition 2 forms)]
    (swap! swap-atom swap-key k v)))

(defn dist [a b]
  (Math/sqrt (+ (Math/pow (- (.getX a) (.getX b)) 2) (Math/pow (- (.getY a) (.getY b)) 2))))
