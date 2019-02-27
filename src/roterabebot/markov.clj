(ns roterabebot.markov)

(defn get-data []
  (->
   (slurp "training_data.txt")
   (clojure.string/split #"\s+")
   ))

(defn parse-data []
  (->
   (get-data)))


(defn get-next-words [chain key]
  (if (contains? chain key)
    (rand-nth (get chain key))
    nil))


(defn build-markov []
  (reduce (fn [chain words]
            (assoc chain (first words) (conj (get chain (first words)) words)))
          {}
          (partition-all 3 1 (parse-data))))

(get (build-markov) "you")


(get (build-markov) (rand-nth (keys (build-markov))))

(def chain (build-markov))

(defn get-next-key [chain previous-key previous-words]
    (last previous-words))


(defn build-sentence [sentence previous-key previous-words]
  (let [key (get-next-key chain previous-key previous-words)
        words (get-next-words chain key)]
    (if (>= (count sentence) (+ (rand-int 10) 1))
      sentence
      (let [sentence (concat sentence (rest words))]
        (build-sentence sentence key words)))))

(defn generate-message []
  (let [first-key (rand-nth (keys chain))
        first-words (get-next-words chain first-key)]
   (build-sentence first-words first-key first-words)))

;; (generate-message)
