(defproject roterabebot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clack "0.1.0"]
                 [http-kit "2.3.0"]
                 [clojure-opennlp "0.5.0"]
                 [clojure-future-spec "1.9.0-alpha17"]
                 [org.clojure/data.json "0.2.6"]]
  :main roterabebot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
