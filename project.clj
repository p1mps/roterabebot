(defproject roterabebot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clucy "0.4.0"]
                 [org.clojure/core.async "1.3.618"]
                 [clj-http "3.12.3"]
                 [cheshire "5.10.0"]
                 [clojure-opennlp "0.5.0"]
                 [stylefruits/gniazdo "1.2.0"]
                 [diehard "0.10.4"]
                 [mount "0.1.17"]
                 [http-kit "2.6.0"]
                 [clucy "0.4.0"]]
  :main roterabebot.core
  :repositories [["public-github" {:url "git://github.com/p1mps/clack" :protocol :ssh}]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
