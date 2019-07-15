(ns roterabebot.nlp
  (:require [roterabebot.load-data :as load-data]
            [roterabebot.input-parser :as input-parser]
            [clojure.set :as clojure.set]
            [opennlp.nlp :as nlp]))


(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "en-ner-person.bin"))
(def name-tags ["SYM" "NN" "NNS" "NNP" "NNPS"])

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn find-tag-in-sentence [tag sentence-tagged]
  (filter #(= tag (second %)) sentence-tagged))

(defn get-message-names [message]
  (map #(first %)
       (mapcat #(find-tag-in-sentence % message) name-tags)))

(defn hamming-distance [list1 list2]
  (count (clojure.set/intersection (set list1) (set list2))))

(defn get-start-key [chain previous-message]
  (map (fn [element]
         {:distance (hamming-distance previous-message element) :key element})
       (keys chain)))

(defn calculate-hamming-map [chain previous-message]
  (filter #(and (>= (:distance %) 1) (not= (:key %) previous-message))
          (get-start-key chain previous-message)))

(defn get-hamming-maps [chain previous-message]
  (let [tags-message (tag-message (clojure.string/join " " previous-message))
        message-names (get-message-names tags-message)
        hamming-map-names (calculate-hamming-map chain message-names)
        hamming-map (calculate-hamming-map chain previous-message)]
    [hamming-map-names hamming-map]))
