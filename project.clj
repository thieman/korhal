(defproject korhal "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :java-source-paths ["jnibwapi"]
  :main korhal.core
  :aot [korhal.core]
  :jvm-opts ["-Djava.library.path=jnibwapi/release"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.nrepl "0.2.3"]])
