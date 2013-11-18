(ns korhal.tools.util)

(def execution-times (atom []))

(defn swap-key [curr-val k v]
  (merge curr-val {k v}))

(defn swap-keys [swap-atom & forms]
  (doseq [[k v] (partition 2 forms)]
    (swap! swap-atom swap-key k v)))

(defn plural [n]
  (let [n-str (str n)
        processed-str (cond (re-find #"[^a]y$" n-str) (str (apply str (butlast n-str)) "ies")
                            (re-find #"[s]$" n-str) n-str
                            (re-find #"larva$" n-str) (str n-str "e")
                            :else (str n-str "s"))]
    (if (symbol? n) (symbol processed-str) processed-str)))

(defn add-execution-time [ms]
  (swap! execution-times conj ms)
  (let [times @execution-times]
    (when (> (count times) 50)
      (swap! execution-times (partial drop (- (count times) 50))))))

(defn get-average-execution-time []
  (let [times @execution-times]
    (int (/ (apply + times) (count times)))))
