(ns roterabebot.markov
  (:require
   [clojure.string :as string]
   [roterabebot.data :as data]
   [clojure.set :as set]))

(def all-sentences (atom #{}))
(def chain (atom {}))

(defn visited?
  [v coll]
  (some #(= % v) coll))

(defn visited?
  "Predicate which returns true if the node v has been visited already, false otherwise."
  [v coll]
  (some #(= % v) coll))

(defn find-neighbors
  "Returns the sequence of neighbors for the given node"
  [v coll]
  (get coll v))


(defn graph-dfs
  "Traverses a graph in Depth First Search (DFS)"
  [graph v]
  (loop [stack   (vector v) ;; Use a stack to store nodes we need to explore
         visited []] ;; A vector to store the sequence of visited nodes
    (if (empty? stack) ;; Base case - return visited nodes if the stack is empty
      visited
      (let [v           (peek stack)
            neighbors   (find-neighbors v graph)
            not-visited (filter (complement #(visited? % visited)) neighbors)
            new-stack   (into (pop stack) not-visited)]
        (if (visited? v visited)
          (recur new-stack visited)
          (recur new-stack (conj visited v)))))))

(defn graph-search
  [coll graph start]
  (loop [coll (conj coll start)
         visited #{}]
    (cond
      (visited (peek coll)) (recur (pop coll) visited)
      :else (let [curr (peek coll)
                  node (graph curr)
                  coll (into (pop coll) (get node curr))
                  visited (conj visited curr)]
              (recur coll visited)))))


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
              (update result words conj following-words)
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
  (->> (let [leaves (get-leaves chain)]
         (for [l leaves]
           ((dfs chain l) [k] #{k})))
       (map flatten)))


(defn get-sentences [chain first-keys]
  (mapcat (fn [k]
            (sentences-by-key k chain))
          first-keys))


(defn generate-sentences [text]
  (println "generating sentences...")
  (let [new-chain  (build-markov (data/generate-text-list text))
        chain (swap! chain merge-with @chain new-chain)
        first-keys (data/generate-first-keys text)
        sentences (remove empty? (get-sentences chain first-keys))
        sentences (set (map #(string/join " " %) sentences))]
    (println "sentences generated...")
    (swap! all-sentences set/union sentences)))
