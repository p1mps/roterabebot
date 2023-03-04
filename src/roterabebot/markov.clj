(ns roterabebot.markov
  (:require
   [clojure.core.reducers :as r]
   [clojure.data :as clj-data]
   [clojure.set :as s]
   [clojure.string :as str]
   [roterabebot.data :as load-data]))


(defn build-markov [data]
  (reduce (fn [result sentence]
            (if (second sentence)
              (update result (first sentence) conj (second sentence))
              (assoc result (first sentence) nil)))
          {}
          data))

(defn remove-at [n coll]
  (concat (take n coll) (drop (inc n) coll)))


(defn find-pred-key [sentences k]
  (map #(hash-map :key-index (.indexOf % k)
                  :sentence-index (.indexOf sentences [k])) sentences))

(defn sentence-by-key [k chain]
  (loop [k k
         chain chain
         sentences []
         all-keys []]
    (let [[next-key & rest-keys] (into all-keys (get chain k))]
      (if next-key
        (recur next-key
               chain
               (conj sentences k)
               rest-keys)
        (conj sentences k)))))


(def chain (atom {}))

(def sentences (atom #{}))

(defn search [s]
  (println "searching answer..." s)
  (into [] (r/filter #(some #{s}
                            (str/split % #" "))
                     @sentences)))

(defn get-sentences [chain first-keys]
  (set (map (fn [k]
              (sentence-by-key k chain))
            first-keys)))


(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (load-data/generate-text-list text))
        diff-chain (clj-data/diff @chain new-chain)
        first-keys (load-data/generate-first-keys text)
        sentences (get-sentences diff-chain first-keys)]
    (println "sentences generated")
    sentences))


(comment

  (reset! sentences nil)
  @sentences
  (search "youtube")
  (search "God")



  (generate-sentences (slurp "test.txt"))

  )
