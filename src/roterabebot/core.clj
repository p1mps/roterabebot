(ns roterabebot.core
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clojure.string :as string]
   [mount.core :as mount]
   [roterabebot.http :as http]
   [roterabebot.markov :as markov]
   [roterabebot.nlp :as nlp]
   [roterabebot.data :as data]
   [roterabebot.socket :as socket]))

(def bot-ids ["U028XHG7U4B" "UFTAL8WH4"])
(def events-messages-received (atom []))



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
  (and (not (some #{(:event_ts message)} @events-messages-received))
       (= "app_mention" (:type message))))

(defn save-message [parsed-message]
  (spit "training_data.txt" (str (:message parsed-message) "\n") :append true))

(defn generate-new-sentences [parsed-message]
  (markov/generate-sentences (:message parsed-message)))

(defn handler [message]
  (let [parsed-message (parse-message message)]
    (println "message received: " parsed-message)

    ;; bot's mention, we reply
    (when (bot-mention? parsed-message)
      ;; keep in memory the timestamp of received messages to not reply to messages already received
      (swap! events-messages-received conj (:event_ts parsed-message))

      (let [reply (nlp/reply parsed-message)]
        (println reply)
        (http/send-message reply)
        (println "removing similar sentences")
        (nlp/reset-sentences reply)))

    ;; drop some messages when there are too many in memory
    (when (> (count @events-messages-received) 100)
      (reset! events-messages-received (drop 50 @events-messages-received)))

    ;; if it's a user message we save it and we just regenerate all our sentences
    (when (user-message? parsed-message)
      (swap! markov/sentences conj (markov/generate-sentences (:message parsed-message)))
      (save-message parsed-message))))


(defn -main
  [& _]
  (markov/generate-sentences (slurp "training_data.txt"))
  (mount/start-with-args {:handler-fn handler
                          :on-close-fn socket/on-close}
                         #'socket/ws-socket))
