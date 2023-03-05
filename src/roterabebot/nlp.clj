(ns roterabebot.nlp
  (:require
   [clojure.string :as string]
   [opennlp.nlp :as nlp]
   [opennlp.tools.filters :as filters]
   [roterabebot.markov :as markov]
   [roterabebot.lucene :as lucene]))

(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-tags #"(NN|NNS|NNP|NNPS|ADJ)")
(def verb-tags #"(VB|VBD|VBG|VBN|VBP|VBZ|IN)")
(def adj-tags #"(JJ)")
(def last-replies (atom []))
(def stopwords
  (set (-> (slurp "stop-words.txt")
           (string/split-lines))))

(def SIMILARITY 0.95)


(filters/pos-filter names-filter name-tags)
(filters/pos-filter verbs-filter verb-tags)
(filters/pos-filter adjs-filter adj-tags)

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn split-tokens [input]
  (-> (string/lower-case input)
      (string/split #"\W+")))

(defn clean-message [message]
  (string/join
   #" "
   (remove stopwords
           (->
            (apply str (filter #(or (Character/isLetter %) (= \space %)) message))
            (string/replace #"\s+" " ")
            (string/trim)
            (string/split #"\W+")))))

(defn random-word [words]
  (rand-nth words))

(defn search [s]
  (println "searching answer..." s)
  (when-not (or (string/blank? s) (= " " s))
    (lucene/search s)))


(defn answer [words]
  (when (not-empty words)
    (let [rand-word (random-word words)
          answers   (search rand-word)]
      (when (not-empty answers)
        (let [rand-answer (rand-nth answers)]
          {:answer rand-answer
           :chosen-word rand-word})))))

(defn random-answer [answers]
  (rand-nth answers))

(defn choose-answer [{:keys [choices previous-message]}]
  (let [reply (->> choices
                   (vals)
                   (map :answer)
                   (filter #(and (not= % previous-message) (not-empty %)))
                   (random-answer))]
    (swap! last-replies conj reply)
    reply))


(defn names [message]
  (map first (names-filter (tag-message message))))


(defn verbs [message]
  (map first (verbs-filter (tag-message message))))

(defn reply [{:keys [message]} sentences]
  (println "finding reply..." message)
  (let [message       (clean-message message)
        words         (string/split message #" ")
        names         (names message)
        verbs         (verbs message)
        name-answer   (answer names)
        verb-answer   (answer verbs)
        word-answer   (answer words)
        reply-data {:previous-message message
                    :names   names
                    :verbs   verbs
                    :words   words
                    :choices {:by-name name-answer
                              :by-verb verb-answer
                              :by-word word-answer
                              :random  {:answer (string/join " " (first (shuffle sentences)))}}}
        reply-data (assoc reply-data :reply (choose-answer reply-data))]
    (clojure.pprint/pprint reply-data)
    reply-data))


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
    (when (> mag 0)
      (/ dot
         mag))))

(defn remove-similar-sentences [sentence sentences]
  (remove
   (fn [s]
     (let [cosine (cosine s sentence)]
       (if (and cosine (>= cosine SIMILARITY))
         true
         false)))
   sentences))

(defn reset-sentences [reply]
  (swap! markov/all-sentences (partial remove-similar-sentences reply)))
