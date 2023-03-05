(ns roterabebot.core
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clojure.core.async :as async]
   [clojure.string :as string]
   [mount.core :as mount]
   [roterabebot.http :as http]
   [roterabebot.lucene :as lucence]
   [roterabebot.markov :as markov]
   [roterabebot.nlp :as nlp]
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
       (= "app_mention" (:type message)))
  )

(defn save-message [parsed-message]
  (spit "training_data.txt" (str (:message parsed-message) "\n") :append true))

(defn handler [message]
  (let [parsed-message (parse-message message)]
    (println "message received: " parsed-message)

    ;; bot's mention, we reply
    (when (bot-mention? parsed-message)
      ;; keep in memory the timestamp of received messages to not reply to messages already received
      (swap! events-messages-received conj (:event_ts parsed-message))

      (let [reply (:reply (nlp/reply parsed-message @markov/all-sentences))]
        (println reply)
        (http/send-message reply)
        (println "removing similar sentences")
        (nlp/reset-sentences reply)
        ))

    ;; drop some messages when there are too many in memory
    (swap! events-messages-received (fn [events]
                                      (if (> (count events) 100)
                                        (drop 50 events)
                                        events)))

    ;; if it's a user message we save it and we just regenerate all our sentences
    (when (user-message? parsed-message)
      (async/thread (lucence/add-sentences! (markov/generate-sentences (:message parsed-message))))
      (save-message parsed-message))))


(defn -main
  [& _]
  (let [sentences (markov/generate-sentences (slurp "training_data.txt"))]
    (println "adding sentences to lucence")
    (async/thread (lucence/add-sentences! sentences))
    (mount/start-with-args {:handler-fn handler
                            :on-close-fn socket/on-close}
                           #'socket/ws-socket)
    ))



(comment
  (lucence/search "stefan"))
