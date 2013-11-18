(ns korhal.tools.util
  (:require [korhal.interop.interop :refer [draw-text]]))

(def execution-times (atom []))

(defn swap-key [curr-val k v]
  (merge curr-val {k v}))

(defn swap-keys [swap-atom & forms]
  (doseq [[k v] (partition 2 forms)]
    (swap! swap-atom swap-key k v)))

(defn add-execution-time [ms]
  (swap! execution-times conj ms)
  (let [times @execution-times]
    (when (> (count times) 50)
      (swap! execution-times (comp vec (partial drop (- (count times) 50)))))))

(defn get-average-execution-time []
  (let [times @execution-times]
    (int (/ (apply + times) (* 1000000 (count times))))))

(defmacro profile [& body]
  `(let [start-time# (System/nanoTime)]
     ~@body
     (add-execution-time (- (System/nanoTime) start-time#))
     (draw-text 525 50 (str "Profiler (ms): " (get-average-execution-time)) true)))
