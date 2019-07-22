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


(t/deftest generate-fixed-message
  (t/is (= nil (markov/generate-fixed-message {(list "message") (list)} (list "message")))))

(t/deftest generate-random-message
  (t/is (= (list "message") (markov/generate-random-message {(list "message") (list)}))))

(def map-values {:1 (list 1 2 3 4),
                 :2 (list 1 2)})

(t/deftest get-num-values
  (t/is (= (markov/get-num-values map-values) (list 4 2))))

(t/deftest get-average-values
  (t/is (= (markov/get-average-values (list 4 2)) 3)))

(t/deftest get-average-values
  (t/is (= (markov/get-average-values (list 4 2)) 3)))

(t/deftest get-chain-average-stats
  (t/is (= (markov/get-chain-average-stats map-values) 3)))

(t/deftest compute-stats
  (t/is (= (markov/compute-stats map-values) {:num-keys 2
                                              :average-values 3})))
;; ("I love spaghet") ("a lot")
;; ("a lot") 
;; ("a lot oh") ("yeah")
;; ("yeah")
;;(markov/update-chain markov-chain update-text)

;; (markov/build-markov
;;  load-data-test/text-list)
(t/run-tests)

