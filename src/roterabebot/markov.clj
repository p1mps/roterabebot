;; TODO filter input properly

(ns roterabebot.markov)

(defn split-sentence [sentence]
  (if (some? sentence)
    (clojure.string/split sentence #"\s+")))

(defn get-data []
  (->
   (slurp "training_data.txt")
   (clojure.string/split #"\s+")
   ))

(defn split-data []
  (->>
   (get-data)
   (partition-all 3 3)
   (partition-all 2 1)
   ))

(defn update-markov [chain data]
  (reduce (fn [chain words]
            (assoc
             chain
             (first words)
             (conj
              (get chain (first words))
              (first (rest words)))))
          {}
          data))


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


(def chain (atom (build-markov)))

(defn update-chain [message]
  (->>
   (clojure.string/split message #"\s+")
   (partition-all 3 3)
   (partition-all 2 1)
   (update-markov @chain)
   ))

(defn not-contains-ends [words]
  (if (empty? words)
    false
    (empty?
     (filter #(= % true)
             (for [w words]
               (= w "end$"))))))

(defn build-sentence [chain sentence previous-key]
  (let [words (rand-nth (get chain previous-key))
        sentence (concat sentence words)]
    (println (rand-nth (get chain previous-key)))
    (println "words")
    (println previous-key)
    ;; (println words)
    ;; (println sentence)
    (if (not-contains-ends words)
      (recur chain sentence words)
      sentence)
    )
  )

(defn message-until-end [coll]
  (take-while #(not (= % "end$")) coll))


(defn filter-previous-message [previous-message user-id]
  (filter #(not= user-id %) previous-message))

(defn get-previous-sentence [previous-message user-id]
  (filter-previous-message
   (split-sentence previous-message) (str "<@" user-id ">")))

(defn hamming-distance [list1 list2]
  (reduce +
          (let [zip (zipmap list1 list2)
                data (first (partition-all 2 1 zip))]
            (mapcat #(if (= (first %) (first (rest %)))
                       (list 1000)
                       (list 0)
                       ) data))))

(defn get-start-key [chain previous-message]
  (->>
   (map (fn [element]
          {:distance (hamming-distance previous-message element) :key element})
    (keys chain))
   (sort-by :distance)
   (reverse)
   ))

(defn generate-message [previous-message user-id]
  (let [previous-message (get-previous-sentence previous-message user-id)
        hamming-map  (get-start-key @chain previous-message)
        start-key-list (take (+ 1 (rand-int 100)) hamming-map)
        random-key (rand-nth (keys @chain))]
    (if (not (empty? start-key-list))
      (let [start-key (:key (rand-nth start-key-list))]
        (some #(when (not= % (list "")) %)
                    (repeatedly
                     #(message-until-end (build-sentence @chain start-key start-key)))))
      (some #(when (not= % (list "")) %)
                  (repeatedly
                   #(message-until-end (build-sentence @chain random-key random-key)))))))


(comment
  (build-sentence @chain '() nil)

  (repeatedly 10
              #(message-until-end
               (build-sentence @chain (list ":dave:") (list ":dave:"))))

  (split-data)

  (get-data)

  (take 5 @chain)

  (keys @chain)
  (vals @chain)

  (get @chain (list "How" "do"))
  (get @chain (list ""))

  (hamming-distance (list "the" "dave") (list "the" "dave"))

  (reverse
   (sort
    (map #(hamming-distance (list "that") %) (keys chain))))

  (get @chain (list "bane" "of" "my"))

  (generate-message ":dave:" ":PD:")

  (reverse
   (get-start-key chain "that"))

  (get-start-key @chain (list "that"))

  (get-previous-sentence {:text ":dave:"} "asdasd")

  )
