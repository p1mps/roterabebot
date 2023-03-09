(ns roterabebot.markov
  (:require
   [clojure.core.async :as async]
   [clojure.core.reducers :as r]
   [clojure.set :as set]
   [clojure.string :as string]
   [roterabebot.data :as data]
   [roterabebot.lucene :as lucene]))

(def all-sentences (atom #{}))
(def chain (atom {}))

(defn reset-all! []
  (reset! all-sentences #{})
  (reset! chain {}))

(defn dfs
  [graph goal]
  (fn search
    [path visited]
    (let [current (peek path)]
      (if (= goal current)
        [path]
        (->> current
             graph
             (remove visited)
             (mapcat #(search (conj path %) (conj visited %))))))))


(def path (atom []))

(defn search-dfs
  [chain v leaf]
  (if (= v leaf)
    @path
    (let [neighbors (get chain v)
          _ (swap! path conj v)]
      (for [n neighbors]
        (search-dfs chain n leaf))
      )))






(defn build-markov [data]
  (reduce (fn [result [words following-words]]
            (if following-words
              (update result words set/union (set [following-words]))
              (if-not (get result words)
                (assoc result words nil)
                result)))
          {}
          data))


(defn get-leaves [chain]
  (reduce (fn [result [k v]]
            (if v
              result
              (conj result k)))
          [] chain))



(defn sentences-by-key [k chain]
  (let [sentence (->> (get-leaves chain)
                      (pmap (fn [l] ((dfs chain l) [k] #{k})))
                      (remove empty?)
                      (pmap flatten))]
    sentence))


(defn create-sentences! [chain first-keys]
  (r/foldcat (r/mapcat (fn [k]
                         (let [sentences (pmap (partial string/join " ") (sentences-by-key k chain))]
                           (doseq [s sentences]
                             (println s)
                             (lucene/add-sentence! s)
                             (swap! all-sentences set/union (set [s])))
                           @all-sentences))
                       first-keys)))


(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (data/generate-text-list text))
        chain (swap! chain merge-with @chain new-chain)
        first-keys (data/generate-first-keys text)]
    (create-sentences! chain first-keys)
    (println "sentences generated...")
    @all-sentences))
