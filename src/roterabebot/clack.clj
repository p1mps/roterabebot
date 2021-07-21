(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [roterabebot.nlp :as nlp]
            [roterabebot.markov :as markov]
            [environ.core :refer [env]]
            [clojure.string :as str]
            [roterabebot.lucene :as lucene])
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

(defn clean-message [previous-message]
  (clojure.string/replace (clojure.string/trim (clojure.string/replace previous-message  #"<@UUNDE8QHY>" "")) #"\s+" " " ))

(def sentences (markov/generate-sentences (slurp "training_data.txt")))

(defn send-ack [msg out-chan my-user-id]
  (println "sending ack")
  (when (is-message? msg my-user-id)
    (let [cleaned-message (clean-message (:text msg))]
      (spit "training.data.txt" cleaned-message :append true)
      (let [reply (nlp/reply cleaned-message)]
        (println "reply " reply)
        (if (not-empty reply)
          (send-message out-chan (:channel msg) (rand-nth reply))
          (do (println "sending random reply")
              (send-message out-chan (:channel msg) (clojure.string/join " " (rand-nth (vec sentences)))))

          )))))



(defn handler
  [in-chan out-chan config]
  (async/go-loop []
    (if-let [msg (async/<! in-chan)]
      (do
        (println "got message" (:text msg))
        (send-ack msg out-chan (:my-user-id config))
        (recur))
      (println "Channel is closed"))))



(defn start-chat []
  (lucene/add-sentences sentences)
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
