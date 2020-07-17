(ns roterabebot.nlp
  (:require [roterabebot.emoji :as emoji]
            [roterabebot.load-data :as load-data]
            [clojure.set :as clojure.set]
            [opennlp.tools.filters :as filters :refer :all]
            [opennlp.nlp :as nlp]))


(def tokenize (nlp/make-tokenizer "en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "en-pos-maxent.bin"))
(def name-find (nlp/make-name-finder "en-ner-person.bin"))
(def get-sentences (nlp/make-sentence-detector "en-sent.bin"))

(def name-tags #"^(WP|WP$|NN|NNS|NNP|NNPS|PRP|PRP$)")
(def verb-tags #"^(VB|VBG|VBD|VBP|VBZ|IN)")



(pos-filter names-filter name-tags)
(pos-filter verbs-filter verb-tags)

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn filter-by-tag [tag tagged-message]
    (cond
      (= tag "names" ) (map first (names-filter tagged-message))
      (= tag "verbs" ) (map first (verbs-filter tagged-message))))



(comment


  (def test-file
    (slurp "training_data.txt"))

  (def txt "This is a cat.\nThis is a dog.\nThis is pizza")


  (def parsed-txt (load-data/generate-text-list txt))


  (def parsed-txt (load-data/generate-text-list test-file))

  parsed-txt

  (defn build-markov [data]
    (reduce (fn [result sentence]
              (reduce (fn [result words]
                        (update result (first words) (comp set conj) (second words)))
                      result
                      sentence))
            {}
            data))

  (def chain (build-markov parsed-txt))


  (get chain (list "them" "i" "think"))
  (get chain (list "tomorrow" "just" "1"))
  (get chain (list "then" "friday" "looks"))
  (get chain (list "free"))
  (get chain '( "in" "London," "ask" ))
  (get chain '( ":dave:" ))
  chain

  ;; anziche lista di liste, mappa e chiavi con sentenza (doppio reduce)
  (defn all-sentences [chain current-key sentence]
    (let [words (get chain current-key)]
      (if (not= #{nil} words)
        (concat sentence
                 (reduce (fn [result w]
                               (all-sentences chain w (concat result current-key)))
                             '()
                             words))

        (concat sentence current-key (list "END$"))
)
      ))




  (all-sentences chain (rand-nth (keys chain)) '())

  (def k (first (keys chain)))

  k
  (def sentences (map #(all-sentences chain % []) (take 100 (keys chain))))

  (def key-sample '( "<@UER2ULNCD>"
               "did"
               "you"))
  (get chain key-sample)

  (remove #(= % '("END$")) (partition-by #(= % "END$") (all-sentences chain key-sample '())))




  (rand-nth (remove #(= % '("$END"))
                    (partition-by #(= % "$END")

                                  (map #(all-sentences chain % '()) (take 100 (keys chain))))))

  (rand-nth (map #(all-sentences chain % '()) (rand-nth (partition-all 10 10 (keys chain)))))
  (take 100 (random-sample 0.5 sentences))


  (rand-nth sentences)

  (filter #(some #{":dave:"} %) (keys chain))


   (filter #(some #{":dave:"} %) sentences)


  (def rand-key (rand-nth (keys chain)))
  (one-sentence chain rand-key rand-key)

  (build-sentence chain)

  (markov/generate-random-message (keys chain) chain)

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
