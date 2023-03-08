(ns roterabebot.data
  (:require [clojure.string :as string]))

(defn split-text-lines-and-remove-nickname
  [text]
  (remove #(string/blank? %)
          (->> (-> (string/replace text "<@UUNDE8QHY>" "")
                   (string/replace #"\n+" "\n")
                   (string/split-lines))
               (map #(-> (string/replace % #"\s+" " ")
                         (string/trim))))))

(defn generate-text-list
  [text]
  (->>
   (split-text-lines-and-remove-nickname text)
   (map #(string/split % #"\s+"))
   (map #(partition-all 3 3 %))
   (mapcat #(partition-all 2 1 %))))

(defn generate-first-keys [text]
  (->>
   (split-text-lines-and-remove-nickname text)
   (map #(string/split % #"\s+"))
   (map #(partition-all 3 3 %))
   (map first)))



(comment
  )
