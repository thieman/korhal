(ns korhal.tools.queue)

(def api-command (atom (clojure.lang.PersistentQueue/EMPTY)))
(def repl-command (atom (clojure.lang.PersistentQueue/EMPTY)))
(def repl-result (atom (clojure.lang.PersistentQueue/EMPTY)))

(defn dequeue!
  [queue]
  (loop []
    (let [q     @queue
          value (peek q)
          nq    (pop q)]
      (if (compare-and-set! queue q nq)
        value
        (recur)))))

(defmacro cmd
  "For REPL use. Wrap a form to be executed during the gameUpdate
  loop."
  [& body]
  `(do (swap! repl-command conj (fn [] (do ~@body)))
       (loop []
         (if-let [result# (dequeue! repl-result)]
           (:result result#)
           (recur)))))

(defn execute-repl-queue []
  (when-let [command (dequeue! repl-command)]
    (try
      (let [result (command)]
        (swap! repl-result conj {:result result}))
      (catch Exception e
        (swap! repl-result conj {:result e})))))
