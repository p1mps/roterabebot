(ns roterabebot.core
  (:gen-class)
  (:require [cheshire.core :refer :all]
            [clj-http.client :as client]
            [clojure.core.async :as async]
            [diehard.core :as dh]
            [gniazdo.core :as ws]
            [roterabebot.lucene :as lucene]
            [roterabebot.markov :as markov]
            [roterabebot.nlp :as nlp]))

(def ws-token (slurp "ws-token.txt"))
(def api-token (slurp "api-token.txt"))
(def channel "CER5J71LY")
(def channel-test "C0291CCA79A")
(def socket (atom nil))
;;(def sentences (clojure.string/split-lines (slurp "s.txt")))
(def bot-ids ["U028XHG7U4B" "UFTAL8WH4"])


(defn send-post [text]
  (client/post "https://slack.com/api/chat.postMessage"
                         {:headers      {"Content-type"  "application/json"
                                         "Authorization" api-token}
                          :form-params  {:channel channel :text text}
                          :content-type :json}))

(defn get-ws-url []
  (-> (client/post  "https://slack.com/api/apps.connections.open"
                          {:headers {"Content-type"  "application/json;charset=UTF-8"
                                     "Authorization" (str "Bearer " ws-token)}})
            :body
            (parse-string true)
            :url))

(declare handler)

(defn clean-message [previous-message]
  (when previous-message
    (->
     previous-message
     (clojure.string/replace #"<@U028XHG7U4B>" "")
     (clojure.string/replace #"\s+" " ")
     (clojure.string/trim))))

(declare get-socket)

(defn get-socket []
  (ws/connect
      (get-ws-url)
    :on-receive handler))


(defn get-message [m]
  (let [e (-> m :payload :event)]
    {:message (clean-message (:text e))
     :type    (:type e)
     :user    (:user e)}))

(defn user-message? [parsed-message]
  (and (not (some #{(:user parsed-message)} bot-ids))  (not-empty (:message parsed-message))))

(def update-data-channel (async/chan))

(defn update-data [parsed-message]
  (when (user-message? parsed-message)
    (spit "training_data.txt" (str (:message parsed-message) "\n") :append true)
    (markov/generate-sentences (:message parsed-message))))


(def handler-channel (async/chan))

(defn handler [message]
  (ws/send-msg @socket message)
  (let [parsed-message (get-message (parse-string message true))]
    (cond
      (= "app_mention" (:type parsed-message))
      (let [reply (nlp/reply parsed-message)]
        (clojure.pprint/pprint reply)
        (send-post (clojure.string/join " " (:reply reply))))
      (= "message" (:type parsed-message))
      (update-data parsed-message)
      (= "disconnect" (:type parsed-message))
      (do
        (println "disconnect event!")
        (reset! socket (get-socket))))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (reset! socket (get-socket))
  (-> (slurp "training_data.txt")
      (markov/generate-sentences))
  )
