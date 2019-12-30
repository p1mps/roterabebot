(ns roterabebot.load-data)

(defn split-text-lines-and-remove-nickname
  [text]
  (-> (clojure.string/replace text "<@UER5B1RMW>" "")
      (clojure.string/split-lines)))

(defn generate-text-list
  [text]
  (->>
   (split-text-lines-and-remove-nickname text)
   (map #(clojure.string/trim %))
   (map #(clojure.string/split % #"\s+"))
   ;; (map #(remove clojure.string/blank? %))
   (map #(partition-all 3 3 %))
   (map #(partition-all 2 1 %))))

(defn generate-first-keys [text]
  (->>
   (split-text-lines-and-remove-nickname text)
   (map #(clojure.string/trim %))
   (map #(clojure.string/split % #"\s+"))
   ;; (map #(remove clojure.string/blank? %))
   (map #(partition-all 3 3 %))
   (map first))
  )

(comment

  (require 'roterabebot.markov)

  (def txt "This is text.\n This is some more text")

  (def parsed-txt (generate-text-list txt))

  (generate-first-keys txt)

  (roterabebot.markov/build-markov parsed-txt)

  )
