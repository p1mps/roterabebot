(ns roterabebot.emoji
  (:require [roterabebot.input-parser :as input-parser]))

(defn is-emoji [string]
  (if (and
       (clojure.string/starts-with? string ":")
       (clojure.string/ends-with? string ":"))
    true
    false))

(defn get-emoji [list]
  (filter #(is-emoji %) list))

(defn get-emojs [message]
  (filter #(get-emoji (list %))) message)

(defn contains-emoji [sentence]
  (not-empty (get-emoji sentence)))

