(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [environ.core :refer [env]]
            [markov-chains.core]
            [clojure.string :as str])
  (:gen-class))

(def data
  (clojure.string/split
   (clojure.string/lower-case
    (slurp "training_data.txt"))  #"\s+"))

(defn generate-message[]
 (take-while #(re-find #"^[a-zA-Z]+" %)
            (take 100 (markov-chains.core/generate (markov-chains.core/collate data 3)))))

(def markov-message
  (first (drop-while empty? (repeatedly generate-message))))

(defn update-training [msg]
    (if (some? msg)
      (spit "training_data.txt" (apply str msg "\n") :append true)))

(defn send-ack [msg out-chan my-user-id]
  (update-training (:text msg))
  (if (and (= (:type msg) "message")
           (not= (:user my-user-id) my-user-id)
           (str/includes? (:text msg) my-user-id))
    (async/go (async/>! out-chan {:type "message"
                                  :channel (:channel msg)
                                   :text message}))
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
