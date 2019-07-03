(ns roterabebot.markov-test
  (:require [roterabebot.markov :as markov]
            [roterabebot.load-data-test :as load-data-test]
            [clojure.test :as t]))

(def markov-chain
  {(list "I" "love" "spaghet") (list (list "a" "lot"))
   (list "a" "lot") (list)
   (list "a" "lot" "oh") (list (list "yeah"))
   (list "yeah") (list)})

(t/deftest build-markov
  ;; words is a list of triplets of words
  ;; chain is the markov chain
  (t/is (= markov-chain
         (markov/build-markov
          load-data-test/text-list))))

(t/deftest update-chain
  (t/is (= markov-chain
         (markov/update-chain
          load-data-test/text))))

;; ("I love spaghet") ("a lot")
;; ("a lot") 
;; ("a lot oh") ("yeah")
;; ("yeah")

(markov/build-sentence markov-chain (list "a" "lot") (list "a" "lot"))
(markov/build-sentence markov-chain (list "a" "lot" "oh") (list "a" "lot" "oh"))
(markov/build-sentence markov-chain (list "I" "love" "spaghet") (list "I" "love" "spaghet"))


(markov/build-markov
 load-data-test/text-list)


(t/run-tests)

