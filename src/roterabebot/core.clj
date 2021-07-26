(ns roterabebot.core
  (:require
   [clj-http.client :as client]
   [roterabebot.nlp :as nlp]
   [gniazdo.core :as ws]
   [cheshire.core :refer :all])
  (:gen-class))

(def ws-token (slurp "ws-token.txt"))
(def api-token (slurp "api-token.txt"))
(def channel "CER5J71LY")
(def channel-test "C0291CCA79A")
(def socket (atom nil))
(def sentences (clojure.string/split-lines (slurp "s.txt")))
(def bot-id "U028XHG7U4B")


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
    (clojure.string/replace (clojure.string/trim (clojure.string/replace previous-message  #"<U028XHG7U4B>" "")) #"\s+" " " )))


(defn on-connect [_]
  (println "Connected to WebSocket."))

(defn on-close [code reason]
  (println "Connection to WebSocket closed.\n"
           (format "[%s] %s" code reason))
  (throw (RuntimeException. "Websocket disconnected")))

(defn on-error [e]
  (println "ERROR:" e))

(defn get-socket []
  (ws/connect
         (get-ws-url)
         :on-connect on-connect
         :on-close on-close
         :on-error on-error
         :on-receive handler))

(defn get-message [m]
  (let [e (-> m :payload :event)]
    ;;(clojure.pprint/pprint m)
    {:message (clean-message (:text e))
     :type (:type e)
     :user  (:user e)}
    ))

(defn handler [message]
  (let [parsed-message (get-message (parse-string message true))]
    (println parsed-message)
    (ws/send-msg @socket message)
    (cond
      (= "app_mention" (:type parsed-message))
      (do
        (println "got menttion!" )
        (let [reply (nlp/reply (:message parsed-message))]
          ;;(println "reply " reply)
          (if (not-empty reply)
            (send-post reply)
            (let [rand-sentence (rand-nth sentences)]
              (println "senting random sentence!")
              (send-post rand-sentence)))))

      (= "message" (:type parsed-message))
      (when (not= (:user parsed-message) bot-id)
        (spit "training_data.txt" (:message parsed-message)))

      (= "disconnect" (:type parsed-message))

      (do
        (println "disconnect event!")
         (reset! socket (get-socket))))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (reset! socket (get-socket)))
