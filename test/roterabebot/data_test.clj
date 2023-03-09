(ns roterabebot.data-test
  (:require [roterabebot.data :as sut]
            [clojure.test :as t]))

(def first-keys
  (sut/generate-first-keys (slurp "test.txt")))


(t/deftest data
  (t/testing "first keys are calculated correctly"
    (t/is (= #{["A" "B" "C"]} first-keys))))
