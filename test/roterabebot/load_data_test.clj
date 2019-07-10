(ns roterabebot.load-data-test
  (:require [roterabebot.load-data :as sut]
            [clojure.test :as t]))

(def text (str "I love spaghet a    lot
                a lot oh yeah"))

(def text-list
  '(((("I" "love" "spaghet") ("a" "lot"))
     (("a" "lot")))
    ((("a" "lot" "oh") ("yeah")) (("yeah")))))

(t/deftest generate-text-list
  (t/is (= text-list (sut/generate-text-list text))))


(t/run-tests)


;; ("I love spaghet") ("a lot")
;; ("a lot") 
;; ("a lot oh") ("yeah")
;; ("yeah")
