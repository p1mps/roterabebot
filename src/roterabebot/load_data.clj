(ns roterabebot.load-data)

(defn split-text-lines-and-remove-nickname
  [text]
  (-> (clojure.string/replace text "<@UUNDE8QHY>" "")
      ;;(clojure.string/replace #"\s+" " ")
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
   (map first)))

(comment

  (require 'roterabebot.markov)

  (def txt "This is a cat.\nThis is a dog.\nThis is pizza")

  (split-text-lines-and-remove-nickname txt)
  (def parsed-txt (generate-text-list txt))

  parsed-txt

  (def first-keys (generate-first-keys txt))

  (roterabebot.markov/build-markov parsed-txt)

  (roterabebot.markov/update-first-keys first-keys "cazzo\nThis is text.")

  )
