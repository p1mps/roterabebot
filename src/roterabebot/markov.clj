;; TODO filter input properly

(ns roterabebot.markov)

(defn divisible-by? [divisor number]
  (zero? (mod number divisor)))

(defn split-sentence [sentence]
  (if (some? sentence)
    (clojure.string/split  sentence #"\s+")))

(defn sentence-divisible-by [sentence]
  (for [number '(3 2)]
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

(defn get-data []
  (->
   (slurp "training_data.txt")
   (clojure.string/split #"\n")
   ))

(defn split-data []
  (->>
   (get-data)
   (map #(clojure.string/split % #"\s+"))
   (divide-sentence)
   (mapcat #(partition-all 2 1 %))
   ))

(defn build-markov []
  (reduce (fn [chain words]
            (assoc
             chain
             (first words)
             (conj
              (get chain (first words))
              (first (rest words)))))
          {}
          (split-data)))


(def chain (build-markov))

(defn not-contains-ends [words]
  (if (empty? words)
    false
    (empty?
     (filter #(= % true)
             (for [w words]
               (= w "end$"))))))

(defn build-sentence [chain sentence previous-key]
  (let [words (rand-nth (get chain previous-key))
        sentence (concat sentence words) ]
    ;; (println "words")
    ;; (println previous-key)
    ;; (println words)
    ;; (println sentence)
    (if (not-contains-ends words)
      (recur chain sentence words)
      sentence)
    )
  )

(defn message-until-end [coll]
  (reduce
   #(let [r (conj %1 %2)]
      (if (clojure.string/includes? %2 "end$") (reduced r) r)) [] coll))


(defn filter-previous-message [previous-message user-id]
  (filter #(not= user-id %) previous-message))

(defn get-previous-sentence [previous-message user-id]
  (filter-previous-message
   (split-sentence previous-message) (str "<@" user-id ">")))

(defn- data-to-string [data]
  (->
   (pr data)
   (with-out-str)))

(defn dump-chain []
  (spit "chain.txt" (data-to-string @chain) ))


(defn hamming-distance [list1 list2]
  (reduce +
          (let [zip (zipmap list1 list2)
                data (first (partition-all 2 1 zip))]
            (mapcat #(if (= (first %) (first (rest %)))
                       (list 1)
                       (list 0)
                       ) data))))


(defn get-start-key [chain previous-message]
  (->>
   (map (fn [element] {:distance (hamming-distance previous-message element) :key element})  (keys chain))
   (sort-by :distance)
   (reverse)
   ;;(sort-by #(:distance %))
   ))

(defn generate-message [previous-message user-id]
  ;; (println previous-message)
  (let [previous-message (get-previous-sentence previous-message user-id)
        hamming-map  (get-start-key chain previous-message)
        start-key (:key (rand-nth (take (rand-int 50) hamming-map)))]
    ;; (println previous-message)
    ;; (println hamming-map)
    ;; (println start-key)
    (build-sentence chain start-key start-key)
    ))



(comment
  (build-sentence chain '() nil)

  (keys chain)


  (hamming-distance (list "the" "dave") (list "the" "dave"))

  (reverse
   (sort
    (map #(hamming-distance (list "that") %) (keys chain))))

  (generate-message "german" ":PD:")

  (reverse
   (get-start-key chain "that"))

  (get-start-key chain (list "that"))

  (get-previous-sentence {:text ":dave:"} "asdasd")

  )
