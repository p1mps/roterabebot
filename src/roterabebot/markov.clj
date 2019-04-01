;; TODO filter input properly

(ns roterabebot.markov)

(defn divisible-by? [divisor number]
  (zero? (mod number divisor)))

(defn split-sentence [sentence]
  (clojure.string/split  sentence #"\s+"))

(defn sentence-divisible-by [sentence]
  (for [number '(5 4 3 2)]
    (do
      (if (divisible-by? number (count sentence))
        number
        ))))

(defn get-sentence-divisors [sentence]
  (let [divisors (sentence-divisible-by sentence)]
    (filter some? divisors)))

(defn divide-sentence [sentence]
  (map #(let [divisors (get-sentence-divisors %)
               divisor (first divisors)]
          (if (nil? divisor)
            (partition-all 3 3 %)
              (partition-all divisor divisor %)))
       sentence))

;; (get-sentence-divisors "asd asd asd asd asd")

(defn get-data-right []
  (->
   (slurp "training_data.txt")
   ;;(clojure.string/replace #"\s+" " ")
   (clojure.string/split #"\n")
   ))

(defn split-data []
  (->>
   (get-data-right)
   (map #(clojure.string/trim %))
   (map #(clojure.string/split % #"\s+"))
   (divide-sentence)
   ))

;; (split-data)

(defn get-partial-map [words]
  (reduce (fn [chain words]
            (assoc
             chain
             (first words)
             (conj (get chain (first words)) words)))
          {}
          words))

(defn build-markov-right []
  (reduce (fn [chain words]
            (merge-with into chain (get-partial-map words)))
          {}
          (split-data)
          ))


;; (build-markov-right)

(def chain (atom (build-markov-right)))

(defn get-data []
  (->
   (slurp "training_data.txt")
   (clojure.string/split #"\s+")
   ))

(defn build-markov []
  (reduce (fn [chain words]
            (assoc
             chain
             (first words)
             (conj (get chain (first words)) words)))
          {}
          (partition-all 3 4 (get-data))))


(defn update-chain-with-words [list-of-words]
  (for [words list-of-words]
    (let [key (first words)]
      (swap! chain assoc key (concat (get chain key) words)))))

(defn update-chain [new-data-line]
  (let [new-data
        (partition-all 3 1 (split-sentence new-data-line))]
          (update-chain-with-words new-data)
    ))

(defn get-next-key [chain previous-key previous-words]
  (let [next-key (last previous-words)]
    (if (= next-key previous-key)
      nil
      next-key)))

(defn get-next-words [chain key]
  (rand-nth (get chain key)))

(defn message-until-dot [coll]
  (reduce
   #(let [r (conj %1 %2)]
      (if (clojure.string/includes? %2 "end$") (reduced r) r)) [] coll))


(defn build-sentence [sentence previous-key previous-words]
  (let [key (get-next-key @chain previous-key previous-words)
        words (get-next-words @chain key)]
    (if (or (nil? key) (some #(= "end$" %) sentence))
      sentence
      (let [sentence (concat sentence (rest words))]
        (println "SENTENCE")
        (println sentence)
        (build-sentence sentence key words)))))

(defn filter-previous-message [previous-message user-id]
  (filter #(not= user-id %) previous-message))

(defn get-first-key [previous-sentence chain]
  (for [word previous-sentence]
     (if (contains? chain word)
       word)))

(defn get-message [first-words first-key]
  (filter #(not= "end$" %)
          (message-until-dot
           (build-sentence first-words first-key first-words))))

(defn get-previous-sentence [previous-message user-id]
  (filter-previous-message
   (split-sentence (:text previous-message)) (str "<@" user-id ">")))

(defn get-words [first-key-random]
  (let [first-key (rand-nth first-key-random)
        first-words (get-next-words @chain first-key)
        words (get-message first-words first-key)]
    (println words)
    (println (count words))
    (if (> (count words) 1)
      words))
  )

(defn generate-message [previous-message user-id]
  (let [previous-sentence (get-previous-sentence previous-message user-id)
        first-key-choices (filter some? (get-first-key previous-sentence @chain))
        first-key-random (rand-nth (keys @chain))
        first-words-random (get-next-words @chain first-key-random)
        random-words (get-message first-words-random first-key-random)]
    ;; (println "FIRST")
    ;; (println previous-sentence)
    ;; (println first-key-choices)
    ;; (println (get @chain (first first-key-choices)))
    ;; (println (get-words first-key-choices))
    (println first-key-random)
    (println first-words-random)
    (println random-words)
    ;; (println "FINE FIRST")
    (if (or (= first-key-choices '(nil)) (empty? first-key-choices))
      random-words
      (let [words (get-words first-key-choices)]
        (if (some? words)
          words
          random-words))
     )))

(defn- data-to-string [data]
  ;; transform the data to a string writer in order to let save it easier to a file
  (->
   (pr data)
   (with-out-str)))

(defn dump-chain []
  (spit "chain.txt" (data-to-string @chain) ))

(dump-chain)

(clojure-version)

;;(not-empty (filter-previous-message (split-sentence "<@UER5B1RMW> to") "<@UER5B1RMW>"))

;; (generate-message {:text ":dave:"} "<@UER5B1RMW>")

;; (get-data-right)

;; (reduce
;;  (fn [new-list data]
;;    (concat new-list (clojure.string/split data #"\s+")))
;; '()
;; (get-data-right))

;; (get-first-key '("PORCO" "DIO") @chain)

;; (let [first-words (get-next-words @chain "PORCO")]
;;    (get-message first-words "PORCO"))

;; (get-next-words @chain ":dave:")

;;(rand-nth (get @chain ":dave:"))

(get @chain "Jack")

;;@chain


;; (rand-nth (get chain "but"))
