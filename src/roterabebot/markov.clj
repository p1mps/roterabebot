;; TODO filter input properly

(ns roterabebot.markov
  (:require [roterabebot.load-data :as load-data]
            [roterabebot.input-parser :as input-parser]
            [clojure.set :as clojure.set]))

(declare build-markov)
(def file (slurp "training_data.txt"))

(defn build-markov [data]
  (reduce (fn [result sentence]
            (reduce (fn [result words]
                      (let [previous-words (get result words)]
                        (if previous-words
                          (update result (first words) (comp distinct conj) (second words))
                          (assoc result (first words) (list (second words)))
  )

                        )
                        )
                    result
                    sentence))
          {}
          data))

(def chain (build-markov (load-data/generate-text-list file)))

(defn sentence-by-key [k chain sentence]
  (let [words    (get chain k)]
    (if words
      (for [w words]
        (sentence-by-key w chain (concat sentence k)))
      (concat sentence k))))

(def sentences
  (map #(flatten (sentence-by-key % chain '())) (keys chain))
    )

(get chain  '("the" "best" "thread" ))
(sentence-by-key '("the" "best" "thread" ) chain '()
                 )

(def data
  (for [s sentences]
    (clojure.string/join " " s)
)
  )
