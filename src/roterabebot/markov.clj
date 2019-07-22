;; TODO filter input properly

(ns roterabebot.markov
  (:require [roterabebot.load-data :as load-data]
             [roterabebot.input-parser :as input-parser]
             [roterabebot.nlp :as nlp]
             [clojure.set :as clojure.set]))



(defn build-markov [data]
  (reduce (fn [chain words]
            (reduce
             (fn [chain words]
               (assoc
                chain (first words)
                (distinct
                 (first (conj (get chain (first words)) (rest words))))))
             chain
             words
             ))
          {}
          data))

(def chain (atom (build-markov (load-data/generate-text-list (slurp "training_data.txt")))))

(defn get-num-values [chain]
  (map #(count (second %)) chain))

(defn get-average-values [vals]
  (float (/ (reduce + vals) (count vals))))

(defn get-chain-average-stats [chain]
  (-> (get-num-values chain)
      (get-average-values)))

(defn compute-stats [chain]
  {:num-keys (count (keys chain))
   :average-values (get-chain-average-stats chain)})

(defn update-chain [chain message]
  (merge-with
   clojure.set/union
   chain
   (build-markov (load-data/generate-text-list message))))

(defn update-chain-atom [message]
  (swap! chain update-chain message))

(defn build-sentence [chain sentence previous-key]
  (let [next-words (get chain previous-key)]
    (if (not-empty next-words)
      (let [rand-words (rand-nth next-words)
            sentence (concat sentence rand-words)]
        (recur chain sentence rand-words))
      sentence)))

(defn generate-random-message [chain]
  (let [random-key (rand-nth (keys chain))
        random-message (build-sentence chain random-key random-key)]
    (if (not (empty? random-message))
      random-message
      random-key)))

(defn get-message-from-hamming-map [chain hamming-map]
  (when (not-empty hamming-map)
    (let [start-key (:key (rand-nth hamming-map))
          sentence (build-sentence chain start-key start-key)]
      sentence)))

(defn generate-fixed-message [chain previous-message]
  (let [hamming-maps (nlp/get-hamming-maps chain previous-message)
        hamming-map-emoji (first hamming-maps)
        hamming-map-names (second hamming-maps)
        hamming-map (get hamming-maps 3)
        message-from-emoji (get-message-from-hamming-map chain hamming-map-emoji)
        message-from-rest (get-message-from-hamming-map chain hamming-map)
        message-from-names (get-message-from-hamming-map chain hamming-map-names)] 
    (cond
      (not-empty message-from-emoji)
      (do
        (println "sending message from emojis")
        message-from-emoji)
      (not-empty message-from-names)
      (do
        (println "sending message from names of previous message")
        message-from-names)
      (not-empty message-from-rest)
      (do
        (println "sending message from random parts of previous message")
        message-from-rest))))
  
    
(defn generate-message [previous-message user-id]
  (let [previous-message (input-parser/get-previous-sentence previous-message user-id)
        message (generate-fixed-message @chain previous-message)]
    (if (and (not (= message previous-message)) (not-empty message))
      message
      (do
        (println "sending completely random message")
        (generate-random-message @chain)))))

