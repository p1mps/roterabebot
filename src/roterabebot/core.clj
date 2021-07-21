(ns roterabebot.core
  (:require [clucy.core :as clucy]
            [roterabebot.markov :as markov]
            [roterabebot.load-data :as load-data]
            [roterabebot.lucene :as lucene]
            [roterabebot.clack :as clack])

  (:gen-class))

(def files (.list (clojure.java.io/file "data")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (clack/start-chat))

(comment

  (-main)


  )
