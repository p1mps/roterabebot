(ns roterabebot.input-parser)

(defn is-emoji [string]
  (if (and
       (clojure.string/starts-with? string ":")
       (clojure.string/ends-with? string ":"))
    true
    false))

(defn get-emoji [list]
  (filter #(is-emoji %) list))

(defn contains-emoji [sentence]
  (not-empty (get-emoji sentence)))

(defn split-sentence [sentence]
  (if (some? sentence)
    (clojure.string/split sentence #"\s+")))

(defn filter-previous-message [previous-message user-id]
  (filter #(not= user-id %) previous-message))

(defn get-previous-sentence [previous-message user-id]
  (filter-previous-message
   (split-sentence previous-message) (str "<@" user-id ">")))

(comment

  (is-emoji ":andea:")

  (is-emoji "foo")

  (list ":andrea:" "foo" "bar")
  (get-emoji (list ":andrea:" "foo" "bar"))

  )
