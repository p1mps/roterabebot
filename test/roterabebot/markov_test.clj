(ns roterabebot.markov-test
  (:require
   [clojure.test :as t]
   [roterabebot.data :as data]
   [roterabebot.markov :as sut]))

(def chain
  (sut/build-markov
   (data/generate-text-list (slurp "test.txt"))))

(t/use-fixtures
  :once
  (fn [f]
    (sut/reset-all!)
    (f)))

(t/deftest markov
  (t/testing "markov chain builds correctly"
    (t/is (= '{("A" "B" "C") #{("D" "E" "G")} ("D" "E" "G") nil}
             chain))))

(t/deftest sentences-by-key
  (t/testing "sentence by A B C"
    (t/is (= '(("A" "B" "C" "D" "E") ("A" "B" "C" "F" "G"))
             (sut/sentences-by-key ["A" "B" "C"]
                                   {["A" "B" "C"] [["D" "E"] ["F" "G"]]
                                    ["D" "E"] nil
                                    ["F" "G"] nil})))

    (t/is (= '(("A" "B" "C" "D" "E" "H" "L") ("A" "B" "C" "F" "G"))
             (sut/sentences-by-key ["A" "B" "C"]
                                   {["A" "B" "C"] [["D" "E"] ["F" "G"]]
                                    ["D" "E"] [["H" "L"]]
                                    ["H" "L"] nil
                                    ["F" "G"] nil})))
    (t/is (= '(("A" "B" "C" "D" "E" "F" "G" "H" "L"))
             (sut/sentences-by-key '("A" "B" "C")
                                   {'("A" "B" "C") '(("D" "E" "F"))
                                    '("D" "E" "F") '(("G" "H" "L"))
                                    '("G" "H" "L") nil})))
    (t/is (= '(("A" "B" "C" "D" "E" "H" "L") ("A" "B" "C" "D" "E" "F" "G"))
             (sut/sentences-by-key ["A" "B" "C"]
                                   {["A" "B" "C"] [["D" "E"]]
                                    ["D" "E"] [ ["F" "G"] ["H" "L"]]
                                    ["H" "L"] nil
                                    ["F" "G"] nil})))

    ))


(t/deftest generate-sentences
  (t/testing "generate sentences works"
    (t/is (= #{"A B C D E G"}
             (sut/generate-sentences (slurp "test.txt"))))
    (t/is (= #{"A B C D E G"}
             @sut/all-sentences)))

  (t/testing "re-generate same sentences doesn't change"
    (t/is (= #{"A B C D E G"}
             (sut/generate-sentences (slurp "test.txt")))))

  (t/testing "add new sentence"
    (t/is (= #{"A B C D E G" "test"}
             (sut/generate-sentences "test")))
    (t/is (= #{"A B C D E G" "test"}
             (sut/generate-sentences "test")))
    (t/is (= #{"A B C D E G" "test"}
             @sut/all-sentences))))
