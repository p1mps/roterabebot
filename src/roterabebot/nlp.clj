(ns roterabebot.nlp
  (:require
   [roterabebot.lucene :as lucene]
   [opennlp.tools.filters :as filters :refer :all]
   [opennlp.nlp :as nlp]))


(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "en-ner-person.bin"))
(def get-sentences (nlp/make-sentence-detector "en-sent.bin"))

(def tags #"^(NN|NNS|NNP|NNPS)")

(pos-filter tags-filter tags)

(defn tag-message [message]
  (pos-tag (tokenize message)))

(def bot-id "U028XHG7U4B")

(defn clean-previous-message [message]
  (->
   (clojure.string/replace message (re-pattern "<.*?>") "")
   (clojure.string/trim)
   (clojure.string/replace #"\s+" " ")
   (clojure.string/replace #"[-]+" " ")
   (clojure.string/replace #"[^a-zA-Z\s]+" "")))


(def last-sentences (atom []))

(defn answer [words]
  (println "finding answer with " words)
  (when (not-empty words)
    (let [rand-word (rand-nth words)
          answers (map :text (lucene/search rand-word))]
      (println "chosen" rand-word)
      (println "possible answers" (count answers))
      (when (not-empty answers)
        (let [rand-answer (rand-nth answers)]
          (when (not (some #{rand-answer} @last-sentences))
            (swap! last-sentences conj rand-answer)
            rand-answer)
          )))))

(defn reply [previous-message]
  (println "finding a reply..")
  (let [cleaned-previous-message (clean-previous-message previous-message)
        interesting-words        (map first (tags-filter (tag-message cleaned-previous-message)))
        words                    (remove empty? (clojure.string/split cleaned-previous-message #" "))
        interesting-answer (answer interesting-words)
        answer (answer words)]
    (println cleaned-previous-message)
    (println "tags" (tags-filter (tag-message cleaned-previous-message)))
    (cond
      (and (> (count interesting-words) 0) (not-empty  interesting-answer))
      interesting-answer
      (and (> (count words) 3) (not-empty answer))
      answer))
  )
