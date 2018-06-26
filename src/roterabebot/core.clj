(ns roterabebot.core
  (:gen-class))

(require 'markov-chains.core)
(require '[clojure.string :as str])

(defn parse-data []
   (clojure.string/split
    (clojure.string/join " "
                  (clojure.string/split
                   (clojure.string/lower-case
                    (slurp "training_data.txt"))
                   #"\d+|\/|\\|\,|\:|\,|\-|left|sociomantic|added|dave|drey|kate|john|matt|fede|stefan|andrea imparato|\?|\!|\+|<media omitted>|\s+"))
    #"\s+"))


(defn run []
  (clojure.string/join " " (take (rand-int 20) (markov-chains.core/generate (markov-chains.core/collate (parse-data) 2)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (run)))
