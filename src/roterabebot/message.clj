(ns roterabebot.message
  (:require [clojure.string :as str]
            [roterabebot.data :as load-data]))

(comment

  (remove-similar-sentences "I love pizza" (take 2 @roterabebot.markov/total-sentences))
  (cosine "i love pizza" "i love pizza dio can")
  )
