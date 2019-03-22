;; TODO filter input properly

(ns roterabebot.markov)

(defn split-sentence [sentence]
  (clojure.string/split sentence #"\s+"))

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

(def chain (atom (build-markov)))

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

(defn generate-message [previous-message user-id]
  (let [previous-sentence (get-previous-sentence previous-message user-id)
        first-key-choices (filter some? (get-first-key previous-sentence @chain))
        first-key-random (rand-nth (keys @chain))]
    (println previous-sentence)
    (println first-key-choices)
    (println first-key-random)
    (if (or (= first-key-choices '(nil)) (empty? first-key-choices))
        (let [first-words-random (get-next-words @chain first-key-random)]
          (get-message first-words-random first-key-random))
        (let [first-key (rand-nth first-key-choices)]
          (let [first-words (get-next-words @chain first-key)]
            (get-message first-words first-key))))))

;;(not-empty (filter-previous-message (split-sentence "<@UER5B1RMW> to") "<@UER5B1RMW>"))

;; (generate-message {:text ":dave:"} "<@UER5B1RMW>")


;; (get-first-key '("PORCO" "DIO") @chain)

;; (let [first-words (get-next-words @chain "PORCO")]
;;    (get-message first-words "PORCO"))

;; (get-next-words @chain ":dave:")

;;(rand-nth (get @chain ":dave:"))

;; (get @chain ":dave:")

;; @chain


;; (rand-nth (get chain "but"))
