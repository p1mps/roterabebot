;; TODO markov chains first keys only
;; update training data file
;; update markov chain

(ns roterabebot.nlp
  (:require [roterabebot.emoji :as emoji]
            [roterabebot.load-data :as load-data]
            [roterabebot.emoji :as emoji]
            [clojure.set :as clojure.set]
            [opennlp.tools.filters :as filters :refer :all]
            [opennlp.nlp :as nlp]
            [roterabebot.markov :as markov]))


(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "en-ner-person.bin"))
(def get-sentences (nlp/make-sentence-detector "en-sent.bin"))

(def name-tags #"^(NN|NNS|NNP|NNPS)")
(def verb-tags #"^(VB|VBD|VBG|VBN|VBP|VBZ)")

(def test-file
  (slurp "training_data.txt"))

(def parsed-txt (load-data/generate-text-list test-file))

(pos-filter names-filter name-tags)
(pos-filter verbs-filter verb-tags)

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn filter-by-tag [tag tagged-message]
    (cond
      (= tag "names" ) (map first (names-filter tagged-message))
      (= tag "verbs" ) (map first (verbs-filter tagged-message))))

(defn filter-by-substring [string]
  (filter #(clojure.string/includes? % string) markov/data))

(defn filter-by-emoji [string]
  (filter #(and (some emoji/is-emoji %) (clojure.string/includes? % string)) markov/data))

(defn filter-by-word [string]
  (for [s (filter #(some #{string} %)
              markov/sentences)]
    (clojure.string/join " " s)))

(defn by-names-and-verbs [tagged-message]
  (let [names (filter-by-tag "names" tagged-message)
        verbs (filter-by-tag "verbs" tagged-message)]
    (map #(filter-by-word %) (concat names verbs))))

(defn by-random-substring [previous-message]
  (map #(filter-by-substring %) (clojure.string/split previous-message #" ")))

(defn by-emoji [previous-message]
  (map #(filter-by-emoji %) (clojure.string/split previous-message #" ")))

(defn random [s level]
  (loop [s s
         level level]
    (if (= level 0)
      s
      (when (and (seq? s) (not-empty s))
        (recur (rand-nth s) (dec level))))))

(defn reply [previous-message]
  (let [tagged-message (tag-message previous-message)
        s (by-names-and-verbs tagged-message)
        rand-s (random s 2)
        s-by-random-string (by-random-substring previous-message)
        rand-s-by-random-string (random s-by-random-string 2)
        s-by-emoji (by-emoji previous-message)
        rand-s-by-emoji (random s-by-emoji 2)
        random-message (rand-nth markov/data)
        choose-random (rand-int 3)]
    (println "previous message" previous-message)
    (println "tagged message" tagged-message)
    (println "by names and verbs" rand-s)
    (println "substring" rand-s-by-random-string)
    (println "random" random-message)
    (println (emoji/is-emoji previous-message))
    (cond
      (and (= choose-random 0) rand-s) rand-s
      (and (= choose-random 1) rand-s-by-random-string) rand-s-by-random-string
      (and (filter emoji/is-emoji (clojure.string/split previous-message #" ")) (= choose-random 2) rand-s-by-emoji) rand-s-by-emoji
      :else random-message)))

(comment

  (random [] 0)
  (random [0] 0)
  (random [[0 1] [2 3]] 2)
  (random [[0 1] [2 3]] 1)
  (random [0 1] 2)

  (random (by-random-substring "youtube") 2)
  (reply "youtube")
  (reply "I prefer boobs")
  (reply "thx")
  (reply "ok")
  (reply "hello you :lips:")
  (filter-by-word "ass")
  (filter-by-word ":dave:")
  (by-emoji ":dave:")

  (when (filter emoji/is-emoji (clojure.string/split ":dave:" #" "))
    true)


  (by-random-substring "ok")
  (filter #(clojure.string/includes? % "ok")
          (list '("ok ciao") '("no")
                )
          )

  (filter-by-tag "names" (tag-message "I prefer boobs"))


  (filter-by-tag "verbs" (tag-message "I prefer boobs"))

  (filter-by-tag "names" (tag-message "one day i went to lidl"))
  (filter-by-tag "verbs" (tag-message "one day i went to lidl"))

  (filter-by-tag "verbs" (tag-message "hello you :lips:"))
  (filter-by-tag "names" (tag-message ":dave:"))


  (def key-sample '( "<@UER2ULNCD>" "did" "you"))

  (defn get-sentence [key chain]
    (concat key (markov/sentence-by-key key chain)))

  (get markov/chain '("will" "setup" "a"))

  (get markov/chain '("test" "case" "for"))
  (get markov/chain '("or" "ass" "for"))

  (get markov/chain '(":dave:"))
  (get markov/chain '("i" "smoked"))
  (get markov/chain '("too" "many"))

  (take 10 markov/chain)

  ;; ((":dave:"))

  (filter-sentences ":dave:")

  (reply ":dave:")
  (reply ":dj:")

  (reply " ")

  (get-sentence (list "YESSSS")
                markov/chain)

  (filter-sentences ":dj:")


  (def txt "This is a cat.\nThis is a dog.\nThis is pizza")

  (def parsed-txt (load-data/generate-text-list txt))


  ;; anziche lista di liste, mappa e chiavi con sentenza (doppio reduce)
  (defn all-sentences [chain current-key sentence]
    (let [words (get chain current-key)]
      (if (not= #{nil} words)
        (concat sentence
                (reduce (fn [result w]
                                   (all-sentences chain w (concat result current-key)))
                                 '()
                                 words))
        (concat sentence current-key (list "END$")))))

  (declare wall-sentences2)

  (defn wall-sentences [chain current-key sentence]
    (let [words (get chain current-key)]
      (if words
        (flatten (map #(wall-sentences chain % (into sentence current-key)) words))
        (if current-key
          (into sentence (list current-key "END$"))
          sentence))))




  ;; (all-sentences chain key-sample '())

  (def key-parts (partition-all 5 5 (keys chain)))

  (def random-keys (take 10 (random-sample 0.5 (keys chain))))

  (defn map-sentences [key-parts]
    (map #(wall-sentences chain % []) key-parts))


  ;; faccio diventare un albero con radice la chiave e poi vado di dfs

  (remove #(= % '("END$"))
                  (partition-by #(= % "END$")

                                (mapcat #(map-sentences %)
                                 key-parts)


                                ))



  (def sentences
    )

  (remove #(= % '("END$")) (
                            partition-by #(= % "END$")
                            (map #(flatten (wall-sentences chain % '())) (keys chain))
                            ))


  (rand-nth (map #(all-sentences chain % '()) (rand-nth (partition-all 10 10 (keys chain)))))
  (take 100 (random-sample 0.5 sentences))


  (rand-nth sentences)

  (filter #(some #{":dave:"} %) (keys chain))


  (filter #(some #{":dave:"} %) sentences)


  (def rand-key (rand-nth (keys chain)))
  (one-sentence chain rand-key rand-key)

  (build-sentence chain)

  (markov/generate-random-message (keys ) chain)

  (def message "I like pizza and hate fascism")


  (def tagged-message '(["I" "PRP"] ["like" "IN"] ["pizza" "NN"] ["and" "CC"] ["hate" "VB"] ["fascism" "NN"]))


  (def markov-chain
    {(list "i" "like" "tv")      ()
     (list "you" "like" "pizza") (list "and" "nothing" "else")})

  markov-chain

  (def markov-keys (keys markov-chain))

  markov-keys
  ;; (("i" "like" "tv") ("you" "like" "pizza"))

  (def message-names  (filter-by-tag "names" tagged-message))
  (def message-verbs  (filter-by-tag "verbs" tagged-message))

  message-names
  ;; ("I" "pizza" "fascism")
  message-verbs
  ;; => ("like" "hate")

  (defn analyze-chain [chain]
    (for [k (keys chain)]
      {k {:names  (filter-by-tag "names"
                                 (tag-message
                                  (clojure.string/join " " k)))
          :verbs  (filter-by-tag "verbs"
                                 (tag-message
                                  (clojure.string/join " " k)))
          ;;emojis
          :values (get chain k)}}))

  (def chain-analyzed (analyze-chain markov-chain))



  chain-analyzed
  ;;    ({("i" "like" "tv")
  ;;      {:names ("i" "tv"),
  ;;       :verbs ("like"),
  ;;       :values ()}}
  ;;     {("you" "like" "pizza")
  ;;      {:names ("you" "pizza"),
  ;;       :verbs ("like"),
  ;;       :values ("and" "nothing" "else")}})

  (defn analyze-message [message]
    {:names   (filter-by-tag "names"
                             (tag-message
                              message))
     :verbs   (filter-by-tag "verbs"
                             (tag-message
                              message))
     ;;emojis
     :message message
     })

  (def analyzed-message (analyze-message message))

  (defn match-keys [message]
    (reduce (fn [chain element]

              )
            {}
            chain-analyzed))

  (match-keys message)

  (conj :e {})
  (defn filter-keys [chain message]
    (let [tagged-message (tag-message message)
          names          (filter-by-tag "names" tagged-message)
          verbs          (filter-by-tag "verbs" tagged-message)
          name-keys      (match-keys chain names)
          verb-keys      (match-keys chain verbs)
          ]
      [name-keys
       verb-keys]))




  analyzed-message
  ;;{:names ("pizza" "fascism"), :verbs ("hate")}
  chain-analyzed
  ;;({:names ("i" "tv"), :verbs (), :key ("i" "like" "tv")} {:names ("pizza"), :verbs (), :key ("you" "like" "pizza")})

  (filter-keys markov-chain message)
  ;; => nil;; => nil


  )
