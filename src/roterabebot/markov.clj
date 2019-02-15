(ns roterabebot.markov)

(defn get-data []
  (remove #(re-matches
            #"((\))|(\()|(-)|(:)|(<media)|(omitted>.)|(\d+:)|(\d+.*)|(\+\d+)|(<.*>.*)|( - : )|(: )|(\d+/\d+/\d+,)|(\d+:\d+)|(added)|(whatsapp.)|(created)|(group)|(\+\d+ \d+)|(sociomantic:)|(fede))" %)
          (clojure.string/split
           (clojure.string/lower-case (slurp "training_data.txt")) #"\s+")
          ))

(defn get-data []
  (->
   (slurp "training_data.txt")
   (clojure.string/lower-case)
   (clojure.string/split #"\s+")
   ))

(defn clean-data [data]
  (remove
   #(re-matches #"((\))|(\()|(-)|(:)|(<media)|(omitted>.)|(\d+:)|(\d+.*)|(\+\d+)|(<.*>.*)|( - : )|(: )|(\d+/\d+/\d+,)|(\d+:\d+)|(added)|(whatsapp.)|(created)|(group)|(\+\d+ \d+)|(sociomantic:)|(fede))" %)
  data))

(defn parse-data []
  (->
   (get-data)
   (clean-data)))

(parse-data)



(defn get-next-words [chain key]
  (rand-nth (get chain key)))


(defn build-markov []
  (reduce (fn [chain words]
            (assoc chain (first words) (conj (get chain (first words)) words)))
          {}
          (partition-all 3 1 (parse-data))))

(get (build-markov) "you")



(get (build-markov) (rand-nth (keys (build-markov))))

(def chain (build-markov))

(defn get-next-key [chain previous-key previous-words]
  (if (nil? previous-key)
    (rand-nth (keys chain))
    (last previous-words)))


(defn build-sentence [sentence previous-key previous-words]
  (let [key (get-next-key chain previous-key previous-words)
        words (get-next-words chain key)]
    (if (some #(= "end$" %) sentence)
      sentence
      (let [sentence (concat sentence (rest words))]
        (build-sentence sentence key words)))))

(defn generate-message []
  (let [first-key (get-next-key chain nil nil)
        first-words (get-next-words chain first-key)]
   (build-sentence first-words first-key first-words)))

;; (generate-message)
