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

(defmacro with-api
  "Used by the AI (not the REPL) to queue commands to be run during
  the next gameUpdate loop. Commands given to your units need to be
  wrapped in this to ensure they run."
  [& body]
  `(do (swap! api-command conj (fn [] (do ~@body)))))

(defmacro cmd
  "For REPL use. Wrap a form to be executed during the gameUpdate
  loop."
  [& body]
  `(do (swap! repl-command conj (fn [] (do ~@body)))
       (loop []
         (if-let [result# (dequeue! repl-result)]
           (:result result#)
           (recur)))))

(defn execute-api-queue []
  (let [command (dequeue! api-command)]
    (when (fn? command)
      (do (command)
          (recur)))))

(defn execute-repl-queue []
  (when-let [command (dequeue! repl-command)]
    (try
      (let [result (command)]
        (swap! repl-result conj {:result result}))
      (catch Exception e
        (swap! repl-result conj {:result e})))))
