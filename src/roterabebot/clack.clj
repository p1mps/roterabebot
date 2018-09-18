(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [environ.core :refer [env]]
            [markov-chains.core]
            [clojure.string :as str])
  (:gen-class))

(def data
  (re-seq #"'|^<|[A-Za-z]+|^>$|\.|\?|:[A-Za-z]+:|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9]\.[^\s]{2,}"
    (slurp "training_data.txt")))

(defn message-until-dot [coll]
  (reduce
   #(let [r (conj %1 %2)]
      (if (re-find #"\.|\n|\?" %2) (reduced r) r)) [] coll))

(defn generate-message[]
  (message-until-dot
               (take 10000 (markov-chains.core/generate (markov-chains.core/collate data 3)))))

(defn markov-message[]
  (clojure.string/join " "
                       (first (drop-while #(or (= "" %) (= % "\n")) (repeatedly generate-message)))))

(defn update-training [msg]
    (if (some? msg)
      (spit "training_data.txt" (apply str msg "\n") :append true)))

(defn is-message? [msg my-user-id]
(and (= (:type msg) "message")
           (some? (:text msg))
           (not= (:user my-user-id) my-user-id)))

(defn send-ack [msg out-chan my-user-id]
  (let [message (markov-message)]
  (when (is-message? msg my-user-id)
    (update-training (:text msg)))
  (when (and (is-message? msg my-user-id) (str/includes? (:text msg) my-user-id)
    (async/go (async/>! out-chan {:type "message"
                                  :channel (:channel msg)
                                  :text message})))
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
