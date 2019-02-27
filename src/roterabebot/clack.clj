(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [roterabebot.markov :as markov]
            [environ.core :refer [env]]
            [markov-chains.core]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

(defn get-data []
  (clojure.string/split
   ((slurp "training_data.txt")) #"\s+")
  )

(defn message-until-dot [coll]
  (reduce
   #(let [r (conj %1 %2)]
      (if (clojure.string/includes? %2 "end$") (reduced r) r)) [] coll))

(defn create-message[]
  (markov/generate-message))


;; (defn generate-message []
;;   (first (drop-while #(>= (+ (rand-int 9) 1) (count %)) (repeatedly create-message))))

(defn count-message-words [message]
  (->
   (clojure.string/join " " message)
   (clojure.string/split #"\W+")
   (count)))

(def answers
  ["yes"
   "no"
   "maybe"
   "who knows"
   "ok"
   "david jack is a cunt"])


(defn generate-simple-answer []
  (answers (rand-int (count answers))))

(count answers)

(count-message-words ["asd?."])

(defn return-answer [message]
  (if (clojure.string/includes? message ":")
    message
    (generate-simple-answer)))


(defn clear-message [message]
  (->
  (clojure.string/join " " message)
  (clojure.string/replace "end$" "")))

(defn generate-message []
  (let [message (create-message)]
    (clear-message message)
    ))

(defn markov-message []
  (generate-message))

(markov-message)

(defn update-training [msg]
  (if (and (some? msg) (not= msg " "))
      (spit "training_data.txt" (apply str msg "\n") :append true)))

(defn is-message? [msg my-user-id]
(and (= (:type msg) "message")
           (some? (:text msg))
           (not= (:user my-user-id) my-user-id)))

(defn send-ack [msg out-chan my-user-id]
  (let [message (markov-message)]
  (when (is-message? msg my-user-id)
    (update-training (:text msg)))
  (when (and
         (is-message? msg my-user-id)
         (or (.contains (:text msg) "roterabe_bot") (str/includes? (:text msg) my-user-id)))
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

(json/write-str "<porco dio>")
