(ns roterabebot.lucene
  (:require
   [clojure.string :as string]
   [clucy.core :as clucy]
   [roterabebot.lucene :as lucene]))

(def index (clucy/memory-index))

(defn add-sentences! [sentences]
  (doseq [s sentences]
    (println s)
    (clucy/add index
               {:sentence s}))
  (println "sentences added to lucene"))


(defn add-sentence! [s]
  (clucy/add index
             {:sentence s}))

(defn search [s]
  (map :sentence (clucy/search index s Integer/MAX_VALUE)))


(defn delete-sentences! [sentences]
  (doseq [s sentences]
    (clucy/delete index
                  {:sentence s})))
