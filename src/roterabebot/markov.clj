(ns roterabebot.markov
  (:require [clojure.set :as set]
            [roterabebot.data :as data]))


(defn build-markov [data]
  (reduce (fn [result [words following-words]]
            (if following-words
              (update result words conj following-words)
              (if-not (get result words)
                (assoc result words '())
                result)))
          {}
          data))



(defn sentence-by-key [k chain]
  (loop [k k
         chain chain
         sentence []
         values (get chain k)]
    (if (empty? values)
      (concat sentence k)
      (let [rand-val (rand-nth values)]
        (recur rand-val
               chain
               (concat sentence k)
               (get chain rand-val))))))


(defn update-markov [message chain]
  (let [new-markov (build-markov (data/generate-text-list message))]
    (merge-with concat chain new-markov)))


;;(merge-with concat '{("test") (), ("test2") (), ("asdasd") ()} '{("test") ()})
