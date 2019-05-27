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
            (assoc
             chain
             (first words)
             (distinct
              (conj
               (get chain (first words))
               (first (rest words))))))
          {}
         data))

(def chain (atom (build-markov (load-data/split-data (slurp "training_data.txt")))))

(defn update-chain [message]
  (->
   (load-data/split-data message)
   (build-markov)))

(defn build-sentence [chain sentence previous-key]
  (let [words (rand-nth (get chain previous-key))
        sentence (concat sentence words)]
    (if (some? words)
      (recur chain sentence words)
      sentence)
    ))

;; (defn hamming-distance [list1 list2]
;;   (reduce +
;;           (let [zip (zipmap list1 list2)
;;                 data (first (partition-all 2 1 zip))]
;;             (mapcat #(if (= (first %) (first (rest %)))
;;                        (list 1)
;;                        (list 0)
;;                        ) data))))

(defn hamming-distance [list1 list2]
  (count (clojure.set/intersection (set list1) (set list2))))

(defn get-start-key [chain previous-message]
  (map (fn [element]
         {:distance (hamming-distance previous-message element) :key element})
       (keys chain)))

(defn get-emojs [message]
  (filter #(input-parser/get-emoji (list %))) message)

(defn generate-random-message [previous-message]
  (let [random-key (rand-nth (keys @chain))
        random-message (build-sentence @chain random-key random-key)]
        (if (not (empty? random-message))
          random-message
          (generate-random-message previous-message))))

(defn find-tag-in-sentence [tag sentence-tagged]
  (filter #(= tag (second %)) sentence-tagged))

(defn get-message-names [message]
  (map #(first %)
       (mapcat #(find-tag-in-sentence % message) name-tags)))

(defn tag-message [message]
  (pos-tag (tokenize message)))

(defn get-message-from-hamming-map [previous-message]
  (let [hamming-map (filter #(> (:distance %) 0) (get-start-key @chain previous-message))
        start-key (:key (rand-nth hamming-map))
        sentence (build-sentence @chain start-key start-key)]
    sentence))

(defn generate-fixed-message [previous-message]
  (let [tags-message (tag-message (clojure.string/join " " previous-message))
        message-names (get-message-names tags-message)]
    (println "message names: " message-names)
    (if (not (empty message-names))
      (get-message-from-hamming-map message-names)
    (get-message-from-hamming-map previous-message))))

;;(get-message-from-hamming-map (list "how's" "life"))

;; (generate-fixed-message (list "name" "cazzo"))

;; (generate-fixed-message (list ":dave:" "cazzo"))

;; (tag-message (clojure.string/join " " (list "my" "name")))

;; (generate-fixed-message (list ":dave:"))

;; (defn generate-message-hamming-map [previous-message]
;;   (let [hamming-map  (filter #(> (:distance %) 0) (get-start-key @chain previous-message))]
;;     (if (not-empty hamming-map)
;;       (let [start-key (:key (rand-nth hamming-map))
;;             sentence (build-sentence @chain start-key start-key)]
;;         (if (not (empty? sentence))
;;           sentence
;;           (generate-message-hamming-map previous-message)))
;;       (generate-random-message previous-message))))

;; (input-parser/get-emoji (list ":dave:" "yo"))

(defn generate-message [previous-message user-id]
  (let [previous-message
        (input-parser/get-previous-sentence previous-message user-id)
        number-words-previous-message (count previous-message)]
    (println previous-message)
    (if (or (> number-words-previous-message 0)
               (input-parser/contains-emoji previous-message))
      (generate-fixed-message previous-message)
      (generate-random-message previous-message)
      )))


(comment
  (build-sentence @chain '() nil)

  (generate-random-message "")

  (count '( penis))

  (split-data)

  (get-data)

  (take 2 @chain)

  (keys @chain)

  (take 10
        (reverse
         (sort
          (map #(count %) (keys @chain)))))

  (vals @chain)

  @chain

  (get @chain (list "How" "do"))
  (get @chain (list ":dave:" "end$"))
  (get @chain (list "<@UER5B1RMW>") )

  (filter (fn [element] (some #(= % "<@UER5B1RMW>") element)) (keys @chain))

  (hamming-distance (list "the" "dave") (list "the" "dave"))

  (reverse
   (sort
    (map #(hamming-distance (list "that") %) (keys chain))))

  (get @chain (list "bane" "of" "my"))
  (get @chain (list "spies" "across" "the"))

  (generate-message ":happystefanwatches:" ":PD:")

  (generate-message ":dave:" ":PD:")

  (reverse
   (get-start-key chain "that"))

  (get-start-key @chain (list "that"))

  (get-previous-sentence {:text ":dave:"} "asdasd")

  (update-chain ":dave:")

  ;; openllp
  (use 'opennlp.nlp)
  (def tokenize (make-tokenizer "en-token.bin"))
  (def pos-tag (make-pos-tagger "en-pos-maxent.bin"))
  (def name-find (make-name-finder "en-ner-person.bin"))


  (def sentence-tagged (pos-tag (tokenize "My name is Lee, not john.")))

  (name-find (tokenize "My name is Lee, not john."))

  (def name-tags ["NN" "NNS" "NNP" "NNPS" "PRP"])

  sentence-tagged

  (filter #(contains? name-tags (second %)) sentence-tagged )

  (defn find-tag-in-sentence [tag]
    (filter #(= tag (second %)) sentence-tagged))

  (map #(first %)
       (mapcat #(find-tag-in-sentence %) name-tags))

  name-tags

  )
