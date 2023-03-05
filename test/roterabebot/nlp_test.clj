(ns roterabebot.nlp-test
  (:require
   [clojure.test :as t]
   [roterabebot.markov :as markov]
   [roterabebot.lucene :as lucene]
   [roterabebot.nlp :as sut]))


(def message
  {:message "I love pizza"})


(def sentences
  '(("pizza" "is" "love")
    ("no")))


(t/use-fixtures
  :once
  (fn [f]
    (lucene/add-sentences! sentences)
    (f)
    ))


(t/deftest clean
    (t/testing "cleaning message"
      (t/is (= "aaaa bb aaa" (sut/clean-message "aaaa  5341 bb \n aaa")))))


(t/deftest names
  (t/testing "name"
    (t/is (= '("pizza" "no") (sut/names "pizza 123 no")))))

(t/deftest verbs
  (t/testing "verb"
    (t/is (= '("love") (sut/verbs "I love pizza")))
    (t/is (= '("like") (sut/verbs "I like pizza")))))


(t/deftest tag
  (t/testing "tagging works"
    (t/is (= '(["Andrea" "NNP"]
               ["likes" "VBZ"]
               ["pizza" "NN"]
               ["," ","]
               ["pasta" "NN"]
               ["and" "CC"]
               ["potatoes" "NNS"]
               ["a" "DT"]
               ["lot" "NN"]) (sut/tag-message "Andrea likes pizza, pasta and potatoes a lot")))))




(t/deftest reply
  (t/testing "reply works"
    (with-redefs [sut/random-answer (fn [_] "love")
                  sut/random-word (fn [_] "love")]
      (let [reply  (sut/reply message sentences)]
        (t/is (=  true (contains? reply :reply)))
        (t/is (=  true (some? (-> reply :choices :random))))
        (t/is (= {:previous-message "love pizza",
                  :names '("love" "pizza"),
                  :verbs '(),
                  :words ["love" "pizza"],
                  :choices
                  {:by-name {:answer "pizza is love"
                             :chosen-word "love"}
                   :by-verb nil,
                   :by-word {:answer "pizza is love"
                             :chosen-word "love"}}}
                 (-> (dissoc reply :reply)
                     (update :choices dissoc :random))))))))

(t/deftest search
  (t/testing "searching M"
    (t/is (= '("pizza is love")
             (sut/search "pizza")))))


(comment

  (def training-senteces
    (markov/generate-sentences (slurp "training_data.txt")))

  )
