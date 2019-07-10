;; TODO filter input properly

(ns roterabebot.markov
  (:require [roterabebot.load-data :as load-data]
             [roterabebot.input-parser :as input-parser]
             [clojure.set :as clojure.set]
             [opennlp.nlp :as nlp]))

(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "en-ner-person.bin"))
(def name-tags ["SYM" "NN" "NNS" "NNP" "NNPS"])


(def sentence-tagged (pos-tag (tokenize "My name is Lee, not john.")))

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

(defn hamming-distance [list1 list2]
  (count (clojure.set/intersection (set list1) (set list2))))

(defn get-start-key [chain previous-message]
  (map (fn [element]
         {:distance (hamming-distance previous-message element) :key element})
       (keys chain)))

(defn get-emojs [message]
  (filter #(input-parser/get-emoji (list %))) message)

(defn generate-random-message [chain previous-message]
  (let [random-key (rand-nth (keys chain))
        random-message (build-sentence chain random-key random-key)]
        (if (not (empty? random-message))
          random-message
          (generate-random-message chain previous-message))))

(defn find-tag-in-sentence [tag sentence-tagged]
  (filter #(= tag (second %)) sentence-tagged))

(defn get-message-names [message]
  (map #(first %)
       (mapcat #(find-tag-in-sentence % message) name-tags)))

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn calculate-hamming-map [chain previous-message]
  (filter #(>= (:distance %) 1) (get-start-key chain previous-message)))

(defn get-message-from-hamming-map [chain previous-message hamming-map]
  (let [start-key (:key (rand-nth hamming-map))
        sentence (build-sentence chain start-key start-key)]
    sentence))

(defn generate-fixed-message [chain previous-message]
  (let [tags-message (tag-message (clojure.string/join " " previous-message))
        message-names (get-message-names tags-message)
        hamming-map-names (calculate-hamming-map chain message-names)
        hamming-map (calculate-hamming-map chain previous-message)]
    (if (and (not (empty? message-names))
             (not (empty? hamming-map-names)))
      (get-message-from-hamming-map chain message-names hamming-map-names)
      (when (not (empty? hamming-map))
        (get-message-from-hamming-map chain previous-message hamming-map)))))



(defn generate-message [previous-message user-id]
  (let [previous-message (input-parser/get-previous-sentence previous-message user-id)
        message (generate-fixed-message @chain previous-message)]
    (if (not (empty? message))
      message
      (generate-random-message @chain previous-message))))

