(ns roterabebot.emoji
  (:require [roterabebot.input-parser :as input-parser]))

(defn get-emojs [message]
  (filter #(input-parser/get-emoji (list %))) message)

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


