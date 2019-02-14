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



(defn find-next-words [chain word]
  (rand-nth (get chain word)))


(defn build-markov []
  (reduce (fn [chain words]
            (assoc chain (first words) (conj (get chain (first words)) words)))
          {}
          (partition-all 3 1 (parse-data))))

(get (build-markov) "you")



(get (build-markov) (rand-nth (keys (build-markov))))

(defn build-sentence [sentence]
  (let [chain (build-markov)
        key (rand-nth (keys chain))
        words (find-next-words chain key)
        ]
    (if (some #(= "end$" %) sentence)
      sentence
      (let [sentence (concat sentence words)]
        (build-sentence sentence)))))

(defn generate-message []
   (build-sentence '()))
