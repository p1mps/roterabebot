(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [roterabebot.markov :as markov]
            [environ.core :refer [env]]
            [clojure.string :as str]
  (:gen-class)))

(defn clear-message [message]
  (->
  (clojure.string/join " " message)
  (clojure.string/replace "end$" "")))

(defn remove-nick [message]
  (clojure.string/trim
   (clojure.string/replace (clojure.string/trim message) #"<@UER5B1RMW>" "")))

(defn generate-message [msg user-id]
  (let [message (markov/generate-message msg user-id)]
    (clear-message message)))

(defn get-stats []
  (let [stats (markov/compute-stats @markov/chain)]
    (str "my stats bitch!\n num-keys: "
         (:num-keys stats)
         "\n average-values: "
         (:average-values stats)
         "\n max: "
         (:max stats)
         "\n min: "
         (:min stats))))

(defn update-training [msg]
  (when (and (some? msg) (not= msg " "))
    (println "got message " msg)
    (spit "training_data.txt" (str msg "\n") :append true)))

(defn is-message? [msg my-user-id]
  (and (= (:type msg) "message")
       (some? (:text msg))
       (not= (:user msg) my-user-id)))

(defn send-message [out-chan channel message]
  (async/go (async/>! out-chan {:type "message"
                                :channel channel
                                :text message})))

(defn send-ack [msg out-chan my-user-id]
  (when (is-message? msg my-user-id)
    (do
      (update-training (remove-nick (:text msg)))
      (markov/update-chain-atom (:text msg))))
  (when (and
         (is-message? msg my-user-id)
         (or (.contains (:text msg) "roterabe_bot") (str/includes? (:text msg) my-user-id)))
    (if (= (:text msg) "<@UER5B1RMW> !stats")
      (send-message out-chan (:channel msg) (get-stats))
      (send-message out-chan (:channel msg) (generate-message (:text msg) my-user-id)))))

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
