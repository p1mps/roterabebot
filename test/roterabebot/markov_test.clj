(ns roterabebot.markov-test
  (:require
   [clojure.data :as clj-data]
   [clojure.walk :as walk]
   [roterabebot.data :as data]
   [roterabebot.markov :as markov]))

(def chain
  (markov/build-markov
   (data/generate-text-list (slurp "test.txt"))))


(def first-keys
  (data/generate-first-keys (slurp "test.txt")))



(markov/get-sentences chain first-keys)


(markov/sentence-by-key ["A" "B" "C"] {["A" "B" "C"] nil})

(mapcat flatten
     (markov/sentence-by-key ["A" "B" "C"] {["A" "B" "C"] [["D" "E"]]
                                            ["D" "E"] nil}))

(markov/sentence-by-key ["A" "B" "C"] {["A" "B" "C"] [["D" "E"]]
                                       ["D" "E"] [["F" "G"]]
                                       ["F" "G"] nil})




(markov/sentence-by-key ["A" "B" "C"] {["A" "B" "C"] [["D" "E"] ["F" "G"]]
                                       ["D" "E"] nil
                                       ["F" "G"] [["H" "L"]]
                                       ["H" "L"] nil})






(map #(.indexOf % ["D" "E"]) [[["A" "B" "C"]]  [["D" "E"] ["F" "G"]]])




[["A" "B" "C"]
 ["D" "E"]]

["D" "E"]

["F" "G"]
