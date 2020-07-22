(ns roterabebot.mind
  (:require [roterabebot.nlp :as nlp-r]
            ))

(defn reply [previous-message]
  (let [tagged-message (nlp-r/tag-message previous-message)
        names (nlp-r/filter-by-tag "names" tagged-message)]
    (map #(nlp-r/filter-sentences %) names)))

(comment



  )
