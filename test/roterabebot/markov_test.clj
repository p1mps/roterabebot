(ns roterabebot.markov-test
  (:require [roterabebot.markov :as markov]
            [roterabebot.load-data :as load-data]
            [roterabebot.load-data-test :as load-data-test]
            [clojure.test :as t]))

(def markov-chain 
  {(list "I" "love" "spaghet") (list (list "a" "lot"))
   (list "a" "lot") (list)
   (list "a" "lot" "oh") (list (list "yeah"))
   (list "yeah") (list)})

(def markov-chain-updated
  {(list "I" "love" "spaghet") (list (list "and" "pizza") (list "a" "lot"))
   (list "a" "lot") (list)
   (list "a" "lot" "oh") (list (list "yeah"))
   (list "yeah") (list)
   (list "and" "pizza") (list)})

(def update-text "I love spaghet and pizza")

(t/deftest build-markov
  ;; words is a list of triplets of words
  ;; chain is the markov chain
  (t/is (= markov-chain (markov/build-markov load-data-test/text-list))))

(t/deftest update-chain
  (t/is (= markov-chain-updated (markov/update-chain markov-chain update-text))))

;; ("I love spaghet") ("a lot")
;; ("a lot") 
;; ("a lot oh") ("yeah")
;; ("yeah")
;;(markov/update-chain markov-chain update-text)

;; (markov/build-markov
;;  load-data-test/text-list)


(t/run-tests)

