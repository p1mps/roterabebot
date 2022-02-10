;; TODO filter input properly

(ns roterabebot.markov
  (:require [roterabebot.load-data :as load-data]
            [clojure.core.reducers :as r]
            [clojure.data :as clj-data]
            [clojure.set :as s]))

(defn build-markov [data]
  (reduce (fn [result sentence]
            (reduce (fn [result words]
                      (update result (first words) conj (second words))
                      )
                    result
                    sentence))
          {}
          data))



(defn remove-at [n coll]
  (concat (take n coll) (drop (inc n) coll)))

(defn sentence-by-key [k chain sentence]
  (loop [k k more [] sentence sentence chain chain]
    (let [[word & words] (into more (get chain k))]
      (if word
        (recur word words (into sentence k) (update chain k (fn [e]
                                                              (remove-at (.indexOf e word) e) )))
        (into sentence k)))))

(def chain (atom {}))

(def total-sentences (atom #{}))

(defn search [s]
  (into [] (r/filter #(clojure.string/includes? % s) @total-sentences)))

(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (load-data/generate-text-list text))
        diff-chain (clj-data/diff @chain new-chain)
        sentences (set (map #(sentence-by-key % (second diff-chain) []) (keys new-chain)))]
    (reset! total-sentences (s/union sentences @total-sentences)))
  (println "sentences generated"))




(comment

  (search "youtube")

  )
