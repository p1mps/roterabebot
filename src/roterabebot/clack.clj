(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [roterabebot.markov :as markov]
            [environ.core :refer [env]]
            [markov-chains.core]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

(defn clear-message [message]
  (->
  (clojure.string/join " " message)
  (clojure.string/replace "end$" "")))

(defn generate-message [msg user-id]
  (let [message (markov/generate-message msg user-id)]
    (clear-message message)
    ))

(defn update-training [msg]
  (if (and (some? msg) (not= msg " "))
      (spit "training_data.txt" (apply str msg " end$\n") :append true)))

(defn is-message? [msg my-user-id]
  (and (= (:type msg) "message")
       (some? (:text msg))
       (not= (:user msg) my-user-id)))

(defn send-ack [msg out-chan my-user-id]
  (when (is-message? msg my-user-id)
    (do
      (update-training (:text msg))
      ;;(markov/update-chain (:text msg))
      ))
  (when (and
         (is-message? msg my-user-id)
         (or (.contains (:text msg) "roterabe_bot") (str/includes? (:text msg) my-user-id)))
    (let [message (generate-message (:text msg) my-user-id)]
    (async/go (async/>! out-chan {:type "message"
                                  :channel (:channel msg)
                                  :text message})))
    ))

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
