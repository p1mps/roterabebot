(ns roterabebot.markov
  (:require
   [clojure.string :as string]
   [roterabebot.data :as data]
   [clojure.set :as set]))

(def all-sentences (atom #{}))
(def chain (atom {}))


(defn build-markov [data]
  (reduce (fn [result [words following-words]]
            (if following-words
              (update result words conj following-words)
              (assoc result words nil)))
          {}
          data))

(defn sentences-by-key [k chain]
  (println "==============")
  (if (get chain k)
    (->> (loop [k k
                chain chain
                sentences []
                all-keys []]
           (let [[next-key & rest-keys] (into all-keys (get chain k))
                 new-chain (if (get chain k)
                             (update chain k (fn [v]
                                               (drop 1 v)))
                             (dissoc chain k))]
             (println k next-key rest-keys (get chain k))
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
         (map flatten))
    [k]))


(defn get-sentences [chain first-keys]
  (mapcat (fn [k]
            (sentences-by-key k chain))
          first-keys))


(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (data/generate-text-list text))
        chain (swap! chain merge-with @chain new-chain)
        first-keys (data/generate-first-keys text)
        sentences (get-sentences chain first-keys)
        sentences (set (map #(string/join " " %) sentences))]
    (println "sentences generated...")
    (swap! all-sentences set/union sentences)))
