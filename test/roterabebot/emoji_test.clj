(ns roterabebot.emoji-test
  (:require [roterabebot.emoji :as sut]
            [clojure.test :as t]))

(t/deftest is-emoji
  (t/is (= true (sut/is-emoji ":andrea:")))
  (t/is (= false (sut/is-emoji "foo"))))

(t/deftest get-emoji
  (t/is (= (list ":andrea:")) (sut/get-emoji (list ":andrea:" "foo" "bar"))))

(t/run-tests)

