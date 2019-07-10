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

(t/deftest build-markov
  ;; words is a list of triplets of words
  ;; chain is the markov chain
  (t/is (= markov-chain (markov/build-markov load-data-test/text-list))))

(t/deftest update-chain
  (t/is (= markov-chain
         (markov/update-chain
          load-data-test/text))))

;; ("I love spaghet") ("a lot")
;; ("a lot") 
;; ("a lot oh") ("yeah")
;; ("yeah")

(def text-double-list "I like pizza margherita
                      I like pizza prosciutto")

(def list-double-list (load-data/generate-text-list text-double-list))

(markov/build-markov
 load-data-test/text-list)

(markov/build-markov
 list-double-list)

list-double-list

(t/run-tests)

