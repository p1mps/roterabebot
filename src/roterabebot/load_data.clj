(ns roterabebot.load-data)

(defn split-text-lines-and-remove-nickname
  [text]
  (set (remove #(clojure.string/blank? %)
               (->> (-> (clojure.string/replace text "<@UUNDE8QHY>" "")

                        (clojure.string/split-lines))
                    (map #(-> (clojure.string/replace % #"\s+" " ")
                              (clojure.string/trim)))))))

(defn generate-text-list
  [text]
  (->>
   (split-text-lines-and-remove-nickname text)
   (map #(clojure.string/split % #"\s+"))
   (filter #(> (count %) 2))
   (map #(partition-all 2 2 %))
   (map #(partition-all 2 1 %))))

(defn generate-first-keys [text]
  (->>
   (split-text-lines-and-remove-nickname text)
   (map #(clojure.string/split % #"\s+"))
   (map #(partition-all 2 2 %))
   (map first)))



(comment
  (generate-text-list "asd asdas asdasd"))
