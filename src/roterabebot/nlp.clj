;; TODO markov chains first keys only
;; update training data file
;; update markov chain

(ns roterabebot.nlp
  (:require [roterabebot.emoji :as emoji]
            [roterabebot.load-data :as load-data]
            [roterabebot.lucene :as lucene]
            [opennlp.tools.filters :as filters :refer :all]
            [opennlp.nlp :as nlp]
            ))


(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "en-ner-person.bin"))
(def get-sentences (nlp/make-sentence-detector "en-sent.bin"))

(def tags #"^(NN|NNS|NNP|NNPS|ADJ|VB|VBD|VBG|VBN|VBP|VBZ)")

(pos-filter tags-filter tags)

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn clean-previous-message [message]
  (->
   (clojure.string/replace message #"[-]+" " ")
   (clojure.string/replace #"[^a-zA-Z\s]+" "")))

(defn reply [previous-message]
  (println previous-message)
  (let [cleaned-previous-message (clean-previous-message previous-message)
        interesting-words        (map first (tags-filter (tag-message cleaned-previous-message)))
        words                    (clojure.string/split cleaned-previous-message #" ")]
    (println clean-previous-message)
    (println "words" words)
    (println "interesting words" interesting-words)
    (cond
      (not-empty interesting-words)
      (map :text (lucene/search (rand-nth interesting-words)))
      (> (count words) 1)
      (map :text (lucene/search (rand-nth words)))))
  )

(comment


  (rand-nth (clojure.string/split "" #" "))


  )
