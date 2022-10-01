(ns roterabebot.nlp
  (:require
   [clojure.string :as str]
   [opennlp.nlp :as nlp]
   [opennlp.tools.filters :as filters :refer :all]
   [roterabebot.markov :as markov]))

(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-tags #"(NN|NNS|NNP|NNPS|ADJ)")
(def verb-tags #"(VB|VBD|VBG|VBN|VBP|VBZ)")
(def adj-tags #"(JJ)")
(def stopwords
  (set (-> (slurp "stop-words.txt")
           (str/split-lines))))

(def SIMILARITY 0.95)


(pos-filter names-filter name-tags)
(pos-filter verbs-filter verb-tags)
(pos-filter adjs-filter adj-tags)

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn split-tokens [input]
  (-> (str/lower-case input)
      (str/split #"\W+")))

(defn clean-previous-message [message]
  (apply str
         (remove stopwords
                 (->
                  (apply str (filter #(or (Character/isLetter %) (= \space %)) message))
                  (clojure.string/replace #"\s+" " ")
                  (clojure.string/trim)
                  (str/split #"\W+")

                  ))))




(defn answer [words]
  (when (not-empty words)
    (let [rand-word (rand-nth words)
          answers   (markov/search rand-word)]
      (when (not-empty answers)
        (let [rand-answer (rand-nth answers)]
          {:answer rand-answer})))))

(defn choose-answer [{:keys [choices previous-message] :as data}]
  (clojure.pprint/pprint data)
  (let [reply (->> (select-keys choices [:by-name :by-verb :by-word :random])
                   (vals)
                   (map :answer)
                   (filter #(and (not= % previous-message) (not-empty %)))
                   (rand-nth))]
    (if (and reply (not (some #{reply} @last-reply)))
      (do (swap! last-reply conj reply)
          reply)

      "NO RANDOM ANSWERS!!!!!!!!!")))



(defn names-verbs [message]
  [(map first (names-filter (tag-message message)))
   (map first (verbs-filter (tag-message message)))])


(defn reply [{:keys [message]}]
  (println "finding reply..." message)
  (let [message (clean-previous-message message)
        _ (println "cleaned message" message)
        words                    (clojure.string/split message #" ")
        [names verbs]            (names-verbs message)
        name-answer              (answer names)
        verb-answer              (answer verbs)
        word-answer              (answer words)]
    (->
     {:previous-message message
      :names   names
      :verbs   verbs
      :words   words
      :choices {:by-name name-answer
                :by-verb verb-answer
                :by-word word-answer
                :random  {:answer (first (shuffle @markov/sentences))}}}
     (choose-answer))))



(defn mag [v]
  (->> (map #(* % %) v)
       (reduce +)
       Math/sqrt))

(defn dot [a b]
  (->> (map * a b)
       (reduce +)))

(defn cosine [a-string b-string]
  (let [a-tokens (remove stopwords (split-tokens a-string))
        b-tokens (remove stopwords (split-tokens b-string))
        all-tokens (distinct (concat a-tokens b-tokens))
        av (map #(get (frequencies a-tokens) % 0) all-tokens)
        bv (map #(get (frequencies b-tokens) % 0) all-tokens)
        dot (dot av bv)
        mag (* (mag av) (mag bv))]
    (when (> mag 0))
    (/ dot
       mag
       )))

(defn remove-similar-sentences [sentence sentences]
  (remove #(>= (cosine % sentence) SIMILARITY) sentences))

(comment
  (choose-answer (reply {:message "dave is nasty developer"}))


  (reply {:message ":dave: :dave:"})
  (reply {:message "are you crazy"})
  (reply {:message "yes oh"})

  (not (some #{[":dave:" "Your" "needs" "will" "be" "served" "well."]} @last-reply))


  )
