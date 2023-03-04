(ns roterabebot.markov
  (:require
   [clojure.string :as str]
   [roterabebot.data :as data]))

(def all-sentences (atom #{}))
(def chain (atom {}))
(def data (atom #{}))

(defn build-markov [data]
  (reduce (fn [result [words following-words]]
            (if following-words
              (update result words conj following-words)
              (assoc result words nil)))
          {}
          data))

(defn sentences-by-key [k chain]
  (->> (loop [k k
              chain chain
              sentences []
              all-keys []]
         (let [[next-key & rest-keys] (into all-keys (get chain k))
               new-chain (if (get chain k)
                           (update chain k (fn [v]
                                             (drop 1 v)))
                           (dissoc chain k))]
           (if next-key
             (recur next-key
                    new-chain
                    (if (get chain k)
                      (conj sentences k)
                      (conj sentences k ["END"]))
                    (if (not-empty (get new-chain k))
                      (conj rest-keys k)
                      rest-keys))
             sentences)))
       (partition-by #(= % ["END"]))
       (remove #(= % [["END"]]))
       (map flatten)))


(defn search [s]
  (println "searching answer..." s)
  (filter #(some #{s} %) @all-sentences))

(defn get-sentences [chain first-keys]
  (mapcat (fn [k]
            (sentences-by-key k chain))
          first-keys))


(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (data/generate-text-list text))
        chain (swap! chain merge-with @chain new-chain)
        first-keys (data/generate-first-keys text)
        sentences (get-sentences  chain first-keys)]
    (println "sentences generated")
    (reset! all-sentences sentences)))
