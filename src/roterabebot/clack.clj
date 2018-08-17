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
    (if ((complement str/blank?) (:text msg))
    (spit "training_data.txt" (:text msg) :append true)
    (spit "training_data.txt" "\n" :append true)))

(defn send-ack
  [msg out-chan my-user-id]
  (let [message (generate-message)]
  (if (and (= (:type msg) "message")
           (not= (:user my-user-id) my-user-id)
           (str/includes? (:text msg) my-user-id))
     (async/go (async/>! out-chan {:type "message"
                                  :channel (:channel msg)
                                   :text message}))
     (update-training msg))))

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
