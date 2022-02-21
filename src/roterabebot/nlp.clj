(ns roterabebot.nlp
  (:require
   [opennlp.tools.filters :as filters :refer :all]
   [opennlp.nlp :as nlp]

   [roterabebot.markov :as markov]))


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
   (clojure.string/replace #"\s+" " ")
   (clojure.string/trim)))


(def last-sentences (atom #{}))


(defn answer [words]
  (when (not-empty words)
    (let [rand-word (rand-nth words)
          answers   (markov/search rand-word)]
      (when (not-empty answers)
        (let [rand-answer (rand-nth answers)]
          {:rand-word rand-word
           :answer rand-answer})))))

(defn choose-answer [{:keys [choices previous-message] :as data}]
  (let [reply (->> (select-keys choices [:by-name :by-verb :by-adj :default])
                   (vals)
                   (map :answer)
                   (filter #(and (not= % previous-message) (not-empty %)))
                   (first))]
    (if (and reply (not (some #{reply} @last-sentences)))
      (do
        (swap! last-sentences conj reply)
        (assoc data :reply reply))
      (assoc data :reply (-> choices :random :answer)))))


(defn reply [{:keys [message]}]
  (let [cleaned-previous-message (clean-previous-message message)
        words                    (remove empty? (clojure.string/split cleaned-previous-message #" "))
        names                    (map first (names-filter (tag-message cleaned-previous-message)))
        verbs                    (map first (verbs-filter (tag-message cleaned-previous-message)))
        adjs                     (map first (adjs-filter (tag-message cleaned-previous-message)))
        name-answer              (answer names)
        verb-answer              (answer verbs)
        adjs-answer              (answer adjs)
        answer                   (answer words)]
    (->
     {:previous-message (clojure.string/split message #" ")
      :names   names
      :verbs   verbs
      :adjs    adjs
      :words   words
      :choices {:by-name name-answer
                :by-verb verb-answer
                :by-adj  adjs-answer
                :default answer
                :random  {:rand-word :random
                          :answer    (first (shuffle @markov/total-sentences))}}}
     (choose-answer))))




(comment
  (choose-answer (reply {:message "dave is nasty developer"}))


  (reply {:message ":dave: :dave:"})
  (reply {:message "are you crazy"})
  (reply {:message "yes oh"})

  (not (some #{[":dave:" "Your" "needs" "will" "be" "served" "well."]} @last-sentences))


  )
