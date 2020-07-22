(defproject roterabebot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [p1mps/clack "9a470b3c9a346bd61754608bd9bab567c1738626"]
                 [http-kit "2.4.0-alpha6"]
                 [clojure-opennlp "0.5.0"]
                 [clojure-future-spec "1.9.0-alpha17"]
                 [org.clojure/data.json "0.2.6"]]
  :main roterabebot.core
  :repositories [["public-github" {:url "git://github.com/p1mps/clack" :protocol :ssh}]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
