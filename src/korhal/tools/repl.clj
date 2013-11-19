(ns korhal.tools.repl
  (:require [clojure.tools.nrepl.server :as repl]))

(def repl-server (atom nil))
(def repl-control (atom false))

(defn start-repl! [port]
  (reset! repl-server (repl/start-server :port port)))

(defn stop-repl! []
  (repl/stop-server @repl-server)
  (reset! repl-server nil))

(defn repl-control! [bool]
  (reset! repl-control bool))
