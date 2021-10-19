(ns roterabebot.lucene
  (:require [clucy.core :as clucy]
            [clojure.core.reducers :as r]))


(def index (atom []))


(defn add-sentences [sentences]
  (doall (doseq [ss sentences]
           (swap! index conj {:words ss :sentence (clojure.string/join " " ss)}))))


(defn search [s]
  (into [] (r/filter #(some #{s} (:words %)) @index)))
