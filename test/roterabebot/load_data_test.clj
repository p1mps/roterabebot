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

(t/deftest generate-first-keys
  (t/is (= text-list (sut/generate-first-keys text))))


(t/deftest split-text
  (t/is (= ["test test"] (sut/split-text-lines-and-remove-nickname "test <@UUNDE8QHY>\n test"))))
