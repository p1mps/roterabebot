(ns roterabebot.lucene
  (:require [clucy.core :as clucy]))


(def index (clucy/disk-index "sentences"))


(defn add-sentences [sentences]
  (doseq [ss sentences]
    (let [s (clojure.string/join " " ss)]
      (println s)
      (spit "s.txt" (str s "\n") :append true)
      (clucy/add index
                 {:text s}))))


(defn search [s]
  (clucy/search index s 1000000000))
