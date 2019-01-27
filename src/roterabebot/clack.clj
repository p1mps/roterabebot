(ns roterabebot.clack
  (:require [clack.clack :as clack]
            [clojure.core.async :as async]
            [environ.core :refer [env]]
            [markov-chains.core]
            [clojure.string :as str])
  (:gen-class))

(defn get-data []
  (remove #(re-matches
            #"((\()|(-)|(:)|(<Media)|(omitted>.)|(\d+:)|(\d+.*)|(\+\d+)|(<.*>.*)|( - : )|(: )|(\d+/\d+/\d+,)|(\d+:\d+)|(added)|(whatsapp.)|(created)|(group)|(\+\d+ \d+)|(sociomantic:)|(fede))" %)
  (clojure.string/split
   (clojure.string/lower-case (slurp "training_data.txt")) #"\s+")
  ))

(defn message-until-dot [coll]
  (reduce
   #(let [r (conj %1 %2)]
      (if (clojure.string/includes? %2 "end$") (reduced r) r)) [] coll))

(defn create-message[]
  (message-until-dot
   (take 100 (markov-chains.core/generate (markov-chains.core/collate (get-data) 3)))))

;; (defn generate-message []
;;   (first (drop-while #(>= (+ (rand-int 9) 1) (count %)) (repeatedly create-message))))

(defn count-message-words [message]
  (->
   (clojure.string/join " " message)
   (clojure.string/split #"\W+")
   (count)))

(count-message-words ["asd?."])

(defn return-answer []
  "oooooook")

(defn generate-message []
  (let [message (create-message)]
    (if (= (count-message-words message) 1)
      (return-answer)
      (clear-message message)
      )))

(generate-message)

(defn clear-message [message]
  (clojure.string/replace message "end$" ""))

(defn markov-message []
  (clojure.string/join " "
                       (generate-message)))

(defn update-training [msg]
    (if (some? msg)
      (spit "training_data.txt" (apply str msg "END$\n") :append true)))

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
