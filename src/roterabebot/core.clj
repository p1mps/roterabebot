(ns roterabebot.core
  (:gen-class))

(use 'markov-chains.core)

(def three-men-in-a-boat
  (->
    (slurp "308.txt")
    (clojure.string/split #"\s+")
    (collate 2)))

(def run
  (->>
  (generate three-men-in-a-boat)
  (take 60)
  (clojure.string/join " ")))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println run))
