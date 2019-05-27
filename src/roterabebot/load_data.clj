(ns roterabebot.load-data)

(defn get-data [data]
  (->
   (clojure.string/replace data "<@UER5B1RMW>" "")
   (clojure.string/split-lines)
   ))

(defn split-data [data]
  (->>
   (get-data data)
   (map #(clojure.string/split % #"\s+"))
   (map #(remove clojure.string/blank? %))
   (map #(partition-all 3 3 %))
   (mapcat #(partition-all 2 1 %))
   ))

(comment
  (get-data)

  (split-data (slurp "training_data.txt"))

  )
