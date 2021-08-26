(ns roterabebot.core
  (:require
   [clj-http.client :as client]
   [roterabebot.nlp :as nlp]
   [gniazdo.core :as ws]
   [cheshire.core :refer :all]
   [roterabebot.markov :as markov]
   [clojure.java.io :as io]
   [roterabebot.lucene :as lucene])
  (:gen-class))

(defn delete-recursively [fname]
  (doseq [f (reverse (file-seq (clojure.java.io/file fname)))]
    (clojure.java.io/delete-file f)))


(def ws-token (slurp "ws-token.txt"))
(def api-token (slurp "api-token.txt"))
(def channel "CER5J71LY")
(def channel-test "C0291CCA79A")
(def socket (atom nil))
(def sentences (clojure.string/split-lines (slurp "s.txt")))
(def bot-ids ["U028XHG7U4B" "UFTAL8WH4"])


(defn send-post [text]
  (client/post "https://slack.com/api/chat.postMessage"
                         {:headers      {"Content-type"  "application/json"
                                         "Authorization" api-token}
                          :form-params  {:channel channel-test :text text}
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

(defn on-connect [_]
  (println "Connected to WebSocket."))

(defn on-close [code reason]
  (println "Connection to WebSocket closed.\n"
           (format "[%s] %s" code reason))
  (System/exit -1))

(defn on-error [e]
  (println "ERROR:" e)
  (ws/close @socket)
  (System/exit -1))

(defn get-socket []
  (ws/connect
         (get-ws-url)
         :on-connect on-connect
         :on-close on-close
         :on-error on-error
         :on-receive handler))


(defn get-message [m]
  (let [e (-> m :payload :event)]
    {:message (clean-message (:text e))
     :type (:type e)
     :user  (:user e)}
    ))


(defn handler [message]
  (let [parsed-message (get-message (parse-string message true))]
    (ws/send-msg @socket message)
    (println parsed-message)
    (cond
      (= "app_mention" (:type parsed-message))
      (let [all-replies (nlp/reply parsed-message)
            reply (nlp/choose-answer all-replies)]
        (clojure.pprint/pprint all-replies)
        (println "reply " reply)
        (if (and (not= reply (:message parsed-message)) (not-empty reply))
          (do
            (send-post reply)
            reply)
          (let [rand-sentence (clojure.string/join " " (rand-nth @markov/total-sentences))]
            (println "senting random sentence!")
            (send-post rand-sentence))))
      (= "message" (:type parsed-message))
      (when (and (not (some #{(:user parsed-message)} bot-ids))  (not-empty (:message parsed-message)))
        (println "updating training data")
        (spit "training_data.txt" (str (:message parsed-message) "\n") :append true)
        (-> (markov/generate-sentences (:message parsed-message))
            :new-sentences
            (lucene/add-sentences)))
      (= "disconnect" (:type parsed-message))

      (do
        (println "disconnect event!")
        (reset! socket (get-socket))))))

(comment
  (def choices
    {:choices {:by-name {:rand-word "dave", :answer "weedy :dave: or speedy :dave:"}, :by-verb nil, :by-adj nil, :default nil}})

  (handler
   (generate-string
    {:payload
     {:event {:text "aasd asd"
              :user "user"
              :type "app_mention"}}})))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (when (.exists (io/file "sentences"))
    (delete-recursively "sentences"))
  (-> (slurp "training_data.txt")
      (markov/generate-sentences)
      :sentences
      (lucene/add-sentences))
  (reset! socket (get-socket))
  )
