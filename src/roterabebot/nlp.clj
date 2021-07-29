(ns roterabebot.nlp
  (:require
   [roterabebot.lucene :as lucene]
   [opennlp.tools.filters :as filters :refer :all]
   [opennlp.nlp :as nlp]))


(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-tags #"(NN|NNS|NNP|NNPS|ADJ)")
(def verb-tags #"(VB|VBD|VBG|VBN|VBP|VBZ)")
(def adj-tags #"(JJ)")

(pos-filter names-filter name-tags)
(pos-filter verbs-filter verb-tags)
(pos-filter adjs-filter adj-tags)

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
  (when (not-empty words)
    (let [rand-word (rand-nth words)
          answers   (map :text (lucene/search rand-word))]
      (when (not-empty answers)
        (let [rand-answer (rand-nth answers)]
          (when (not (some #{rand-answer} @last-sentences))
            (swap! last-sentences conj rand-answer)
            {:rand-word rand-word
             :answer rand-answer})

          ))))
  )

(defn reply [{:keys [message]}]
  (println "finding a reply..")
  (let [cleaned-previous-message (clean-previous-message message)
        names        (map first (names-filter (tag-message cleaned-previous-message)))
        verbs        (map first (verbs-filter (tag-message cleaned-previous-message)))
        adjs         (map first (adjs-filter (tag-message cleaned-previous-message)))
        words        (remove empty? (clojure.string/split cleaned-previous-message #" "))
        name-answer (answer names)
        verb-answer (answer verbs)
        adjs-answer (answer adjs)
        answer      (when (> (count words) 2) (answer words))]
    {:names names
     :verbs verbs
     :words words
     :choices {:by-name name-answer
               :by-verb verb-answer
               :by-adj  adjs-answer
               :default answer}})
  )


(comment
  (reply {:message "dave is nasty developer"})

  (reply "dave and stefan are nasty")
  (reply ":sexy-wave: dave")
  (reply "sexy")

  )
