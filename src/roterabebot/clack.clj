(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [environ.core :refer [env]]
            [markov-chains.core]
            [clojure.string :as str])
  (:gen-class))

(defn parse-data []
  (clojure.string/split
   (clojure.string/join " "
                        (clojure.string/split
                         (clojure.string/lower-case
                          (slurp "training_data.txt"))
                         #"\d+|\/|\\|\,|\:|\,|\-|left|sociomantic|added|dave|drey|kate|john|matt|fede|stefan|andrea imparato|\?|\!|\+|<media omitted>|\s+"))
   #"\s+"))

(defn generate-message []
  (clojure.string/join " " (take (rand-int 20) (markov-chains.core/generate (markov-chains.core/collate (parse-data) 3)))))

(defn update-training [msg]
    (if (some? msg)
      (spit "training_data.txt" (apply str msg "\n") :append true)))

(defn send-ack [msg out-chan my-user-id]
  (update-training (:text msg))
  (let [message (generate-message)]
  (if (and (= (:type msg) "message")
           (not= (:user my-user-id) my-user-id)
           (str/includes? (:text msg) my-user-id)
           (not-empty message))
    (async/go (async/>! out-chan {:type "message"
                                  :channel (:channel msg)
                                   :text message}))
     )))

(defn handler
  [in-chan out-chan config]
  (async/go-loop []
    (if-let [msg (async/<! in-chan)]
      (do
        (send-ack msg out-chan (:my-user-id config))
        (recur))
      (println "Channel is closed"))))

(defn start-chat []
  (clack/start (env :slack-api-token) roterabebot.clack/handler))
