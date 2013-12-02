(ns korhal.tools.queue
  (:require [korhal.interop.interop :refer :all]))

(def api-command (atom (clojure.lang.PersistentQueue/EMPTY)))
(def api-defer (atom (clojure.lang.PersistentQueue/EMPTY)))
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
  "Used by the AI to queue commands to be run during the next
  gameUpdate loop. Commands given to your units need to be wrapped in
  this to ensure they run."
  [& body]
  `(do (swap! api-command conj (fn [] (do ~@body)))))

(defmacro with-api-when
  "Used by the AI to queue commands to be run in the next frame where
  test passes. If the test fails, the command is re-queued for the
  next gameUpdate iteration."
  [test & body]
  `(do (swap! api-defer conj {:test (fn [] (do ~test))
                              :command (fn [] (do ~@body))
                              :frame (frame-count)})))

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
      (try
        (command)
        (catch Exception e
          (println "Exception in API queue!")
          (.printStackTrace e)))
      (recur))))

(defn execute-when-queue []
  (let [{:keys [test command frame] :as doc} (dequeue! api-defer)]
    (when doc
        (cond
         (< (frame-count) frame) nil
         (not (test)) (do (try (swap! api-defer conj {:test test :command command :frame (inc frame)})
                               (catch Exception e
                                 (println "Exception in API When queue!")
                                 (.printStackTrace e)))
                          (recur))
         :else (do (try (command)
                        (catch Exception e
                          (println "Exception in API When queue!")
                          (.printStackTrace e)))
                   (recur))))))

(defn execute-repl-queue []
  (when-let [command (dequeue! repl-command)]
    (try
      (let [result (command)]
        (swap! repl-result conj {:result result}))
      (catch Exception e
        (swap! repl-result conj {:result e})))))
