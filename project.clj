(defproject roterabebot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [rm-hull/markov-chains "0.1.1"]
                 [clack "0.1.0"]]
  :main ^:skip-aot roterabebot.core
  :target-path "target/%s"
  :jvm-opts ["--add-modules" "java.xml.bind"]
  :profiles {:uberjar {:aot :all}})
