(ns roterabebot.markov-chain)

(defn split-lines []
  (->
   (slurp "test.txt")
   (clojure.string/split #"end\$")))

(split-lines)

(defn parse-data []
  (->>
   (split-lines)
   (map #(clojure.string/split % #"\s+"))
   (map #(remove empty? %)))


(def chain {})

;; [(word1 word2 word3): next_word]
(defn create-map []
  (->>
   (parse-data)
   (map #(assoc {} (first %) (rest %)) )
   ))



(create-map)
