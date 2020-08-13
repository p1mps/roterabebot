(ns roterabebot.input-parser)


(defn split-sentence [sentence]
  (if (some? sentence)
    (clojure.string/split
     (clojure.string/replace sentence "<@UER5B1RMW>" "") #"\s+")))

(defn filter-previous-message [previous-message user-id]
  (filter #(and (not-empty %) (not= user-id %)) previous-message))

(defn get-previous-sentence [previous-message user-id]
  (filter-previous-message
   (split-sentence previous-message) (str "<@" user-id ">")))
