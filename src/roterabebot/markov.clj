;; TODO filter input properly

(ns roterabebot.markov
  (:require [roterabebot.load-data :as load-data]
            [roterabebot.input-parser :as input-parser]
            [clojure.set :as clojure.set]))

(declare build-markov)
;;(def file (slurp "training_data.txt"))

(defn build-markov [data]
  (reduce (fn [result sentence]
            (reduce (fn [result words]
                      (update result (first words) conj (second words))
                      )
                    result
                    sentence))
          {}
          data))

;;(def chain (atom (build-markov (load-data/generate-text-list file))))

(defn update-markov [chain previous-sentence]
  (merge-with concat chain (build-markov (load-data/generate-text-list previous-sentence))))

;; (defn sentence-by-key [k chain sentence]
;;   (let [words    (get chain k)]
;;     (println words)
;;     (if words
;;       (for [w words]
;;         (sentence-by-key w chain (concat sentence k)))
;;       (concat sentence k))))

(defn remove-at [n coll]
  (concat (take n coll) (drop (inc n) coll)))

(defn sentence-by-key [k chain sentence]
  (loop [k k more [] sentence sentence chain chain]
    (let [[word & words] (into more (get chain k))]
      (if word
        (recur word words (into sentence k) (update chain k (fn [e]
                                                              (remove-at (.indexOf e word) e) )))
        (into sentence k)))))

(defn generate-sentences [text]
  (println "generating sentences...")
  (let [chain (build-markov (load-data/generate-text-list text))
        first-keys (load-data/generate-first-keys text)
        sentences (map #(sentence-by-key % chain []) first-keys)]
    (set sentences)
    ))
