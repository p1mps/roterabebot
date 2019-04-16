(ns roterabebot.load-data)

(defn get-data []
  (->
   (slurp "training_data.txt")
   (clojure.string/replace "<@UER5B1RMW>" "")
   (clojure.string/split #"\s+")
   ))

(defn split-data []
  (->>
   (get-data)
   (partition-all 3 3)
   (partition-all 2 1)
   ))
