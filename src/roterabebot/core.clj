(ns roterabebot.core
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clojure.core.async :as async]
   [clojure.string :as string]
   [mount.core :as mount]
   [roterabebot.data :as data]
   [roterabebot.http :as http]
   [roterabebot.markov :as markov]
   [roterabebot.nlp :as nlp]
   [roterabebot.socket :as socket]))

(def bot-ids ["U028XHG7U4B" "UFTAL8WH4"])
(def chain (atom {}))


(defn clean-message [previous-message]
  (when previous-message
    (->
     previous-message
     (string/replace #"<@U028XHG7U4B>" "")
     (string/replace #"\s+" " ")
     (string/trim))))

(defn parse-message [m]
  (let [e (-> (json/parse-string m true) :payload :event)]
    {:event_ts (:event_ts e)
     :message (clean-message (:text e))
     :type    (:type e)
     :user    (:user e)}))

(defn user-message? [message]
  ;; not a bot nor empty
  (and (= "message" (:type message))
       (not (some #{(:user message)} bot-ids))
       (not-empty (:message message))))

(defn bot-mention? [message]
  (= "app_mention" (:type message)))

(defn save-message [message]
  (spit "training_data.txt" (str message "\n") :append true))

(defn handler [message]
  (let [parsed-message (parse-message message)]
    (println "message received: " (:message parsed-message))

    (when (bot-mention? parsed-message)
      (println "bot mention")
      (let [reply (nlp/reply (:message parsed-message) @chain)]
        (println reply)
        (http/send-message reply)))

    (when (user-message? parsed-message)
      (println "user message")
      (save-message (:message parsed-message))
      ;; messages can arrive at the same time and we don't want to update the chain yet
      (async/thread
        (Thread/sleep 1000)
        (swap! chain (partial markov/update-markov (:message parsed-message)))))))

(defn -main
  [& _]
  (reset! chain (markov/build-markov (data/generate-text-list (slurp "training_data.txt"))))
  (mount/start-with-args {:handler-fn handler
                          :on-close-fn socket/on-close}
                         #'socket/ws-socket))
