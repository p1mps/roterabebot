(ns roterabebot.lucene
  (:require
   [clojure.string :as string]
   [clucy.core :as clucy]))

(def index (clucy/memory-index))

(defn add-sentences! [sentences]
  (doseq [s sentences]
    (clucy/add index
               {:sentence (string/join " " s)}))
  (println "sentences added to lucene"))

(defn search [s]
  (map :sentence (clucy/search index s Integer/MAX_VALUE)))
