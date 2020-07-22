(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [roterabebot.nlp :as nlp]
            [roterabebot.markov :as markov]
            [environ.core :refer [env]]
            [clojure.string :as str])
  (:gen-class))

(defn clear-message [message]
  (->
  (clojure.string/join " " message)
  (clojure.string/replace "end$" "")))

(defn remove-nick [message]
  (clojure.string/trim
   (clojure.string/replace (clojure.string/trim message) #"<@UER5B1RMW>" "")))

(defn update-training [msg]
  (println "got message " msg)
  (when (and (some? msg) (not= msg " "))
    (spit "training_data.txt" (str msg "\n") :append true)))

(def user-id "UUNDE8QHY")

(defn is-message? [msg my-user-id]
  (and (= (:type msg) "message")
       (some? (:text msg))
       (not= (:user msg) my-user-id)
       (clojure.string/includes? (:text msg)
                                 (str "<@" user-id ">"))))

(defn send-message [out-chan channel message]
  (async/go (async/>! out-chan {:type "message"
                                :channel channel
                                :text message})))

(defn send-ack [msg out-chan my-user-id]
  (println "got message " msg)
  (println "my user id " my-user-id)
  (println "user id " (:user msg))
  (println (is-message? msg my-user-id))

  (when (is-message? msg my-user-id)
    (let [reply (nlp/reply (:text msg))]
      (println "reply " reply)
      (send-message out-chan (:channel msg) reply)
      )
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
  (clack/start (env :slack-api-token) roterabebot.clack/handler {:my-user-id user-id}))


(comment
  (send-ack {:text "dave"
             :channel "channeld"}
            (async/chan)
            "1234")
  (nlp/reply
   ":dave:")

  (nlp/reply
   ""))
