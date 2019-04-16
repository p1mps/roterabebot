;; TODO filter input properly

(ns roterabebot.markov
  (:require [roterabebot.load-data :as load-data]
             [roterabebot.input-parser :as input-parser]
             [roterabebot.output-filter :as output-filter]))

(defn build-markov [chain data]
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

(def chain (atom (build-markov {} (load-data/split-data))))

(defn update-chain [message]
  (->>
   (clojure.string/split message #"\s+")
   (partition-all 3 3)
   (partition-all 2 1)
   (build-markov @chain)
   ))

(defn build-sentence [chain sentence previous-key]
  (let [words (rand-nth (get chain previous-key))
        sentence (concat sentence words)]
    (println (rand-nth (get chain previous-key)))
    (println "words")
    (println previous-key)
    (if (output-filter/not-contains-ends words)
      (recur chain sentence words)
      sentence)
    ))

(defn hamming-distance [list1 list2]
  (reduce +
          (let [zip (zipmap list1 list2)
                data (first (partition-all 2 1 zip))]
            (mapcat #(if (= (first %) (first (rest %)))
                       (list 1)
                       (list 0)
                       ) data))))

(defn get-start-key [chain previous-message]
  (map (fn [element]
         {:distance (hamming-distance previous-message element) :key element})
       (keys chain))
  ;;(sort-by :distance)
  ;;(reverse)
  )

(defn generate-fixed-message [previous-message]
  (let [hamming-map  (filter #(> (:distance %) 0) (get-start-key @chain previous-message))
        start-key (:key (rand-nth hamming-map))
        sentence (output-filter/message-until-end
                   (build-sentence @chain start-key start-key))]
    (println hamming-map)
    (if (not (empty? sentence))
      sentence
      (generate-fixed-message previous-message))))

(defn generate-random-message [previous-message]
  (let [random-key (rand-nth (keys @chain))
        random-message (output-filter/message-until-end (build-sentence @chain random-key random-key))]
        (if (not (empty? random-message))
          random-message
          (generate-random-message previous-message))))

(defn generate-message [previous-message user-id]
  (let [previous-message
        (input-parser/get-previous-sentence previous-message user-id)
        number-words-previous-message (count previous-message)]
    (println "previous-message")
    (println previous-message)
    (if (or (= number-words-previous-message 1)
               (input-parser/contains-emoji previous-message))
      (generate-fixed-message previous-message)
      (generate-random-message previous-message)
      )))


(comment
  (build-sentence @chain '() nil)

  (repeatedly 10
              #(message-until-end
               (build-sentence @chain (list ":dave:") (list ":dave:"))))

  (split-data)

  (get-data)

  (take 5 @chain)

  (keys @chain)

  (take 10
        (reverse
         (sort
          (map #(count %) (keys @chain)))))

  (vals @chain)

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

  (generate-message ":dave:" ":PD:")

  (generate-message "david" ":PD:")

  (reverse
   (get-start-key chain "that"))

  (get-start-key @chain (list "that"))

  (get-previous-sentence {:text ":dave:"} "asdasd")

  )
