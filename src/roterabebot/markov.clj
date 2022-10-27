(ns roterabebot.markov
  (:require
   [clojure.core.reducers :as r]
   [clojure.data :as clj-data]
   [clojure.set :as s]
   [clojure.string :as str]
   [roterabebot.data :as load-data]
   [roterabebot.nlp :as nlp]))


(defn build-markov [data]
  (reduce (fn [result sentence]
            (reduce (fn [result words]
                      (if (second words)
                        (update result (first words) conj (second words))
                        (assoc result (first words) nil)
                        ))
                    result
                    sentence))
          {}
          data))

(defn remove-at [n coll]
  (concat (take n coll) (drop (inc n) coll)))

(defn sentence-by-key [k chain sentence]
  (loop [k k
         chain chain
         sentence []
         all-keys []]
    (let [[next-key & rest-keys] (into all-keys (get chain k))]
      (if next-key
        (recur next-key
               (if (empty? (get chain k))
                 (dissoc chain k)
                 (update chain k (fn [v]
                                   (pop v))))
               (apply conj sentence
                     (if (empty? (get chain k))
                       [k "END"]
                       [k]))
               (when-not (empty? (get chain k)) (concat [k] rest-keys)))
        (conj sentence )))))


(def chain (atom {}))

(def sentences (atom #{}))

(defn search [s]
  (println "searching answer...")
  (into [] (r/filter #(some #{s}
                            (str/split % #" "))
                     @sentences)))


(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (load-data/generate-text-list text))
        diff-chain (clj-data/diff @chain new-chain)
        first-keys (load-data/generate-first-keys text)
        sentences (set (mapcat (fn [k]
                              (->> (sentence-by-key k (second diff-chain) [])
                                   (partition-by #(= % "END"))
                                   (remove #{'("END")})
                                   (map flatten)
                                   (map #(str/join #" " %)))) first-keys))]
    (println "sentences generated")
    sentences))

(defn reset-sentences [reply]
  (reset! sentences (nlp/remove-similar-sentences reply @sentences)))

(comment

  (reset! sentences nil)
  @sentences
  (search "youtube")
  (search "God")



  (generate-sentences (slurp "test.txt"))

  )
