(ns roterabebot.nlp
  (:require
   [clojure.string :as string]
   [roterabebot.markov :as markov]))


(def stopwords
  (set (-> (slurp "stop-words.txt")
           (string/split-lines))))

(defn clean-message [message]
  (when message
    (remove stopwords
            (-> message
                (string/replace #"\s+" " ")
                (string/trim)
                (string/split #" ")))))

(defn random-word [words]
  (when-not (or (= words [""]) (empty? words))
    (rand-nth words)))


(defn reply [message chain]
  (println "finding reply..." message chain)
  (let [cleaned-message       (clean-message message)
        word          (random-word cleaned-message)
        starting-keys (when word (filter (fn [ks]
                                           (some #(string/includes? % word) ks)) (keys chain)))
        answer (when-not (empty? starting-keys)
                 (string/join " " (markov/sentence-by-key (rand-nth starting-keys) chain)))]
    (if (and answer (not= answer message))
      answer
      (string/join " " (markov/sentence-by-key (rand-nth (keys chain)) chain)))))

(comment
  (clean-message "dave dave")

  (filter (fn [ks]
            (some #(string/includes? % "a") ks)) ["aabbbb" "b" "c"])

  (reply "test" @roterabebot.core/chain))
