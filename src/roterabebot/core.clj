(ns roterabebot.core
  (:require [cheshire.core :refer :all]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [diehard.core :as dh]
            [gniazdo.core :as ws]
            [roterabebot.lucene :as lucene]
            [roterabebot.markov :as markov]
            [roterabebot.nlp :as nlp])
  (:import java.io.File)
  (:gen-class))

(defn delete-recursively [fname]
  (doseq [f (reverse (file-seq (clojure.java.io/file fname)))]
    (clojure.java.io/delete-file f)))


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
  (dh/with-retry
    {:retry-on          Exception
     :max-retries       10
     :on-retry          (fn [_ _] (prn "Retrying..."))
     :on-failure        (fn [_ _] (prn "Failure!"))
     :on-failed-attempt (fn [_ _] (prn "Failed to reconnect"))
     :on-success        (fn [_] (prn "Connected to WebSocket."))}
    (ws/connect
        (get-ws-url)
      :on-receive handler)))


(defn get-message [m]
  (let [e (-> m :payload :event)]
    {:message (clean-message (:text e))
     :type    (:type e)
     :user    (:user e)}
    ))

(defn bench-f [f text]
  (println text)
  (time f))



(defn handler [message]
  (let [parsed-message (get-message (parse-string message true))]
    (ws/send-msg @socket message)
    (cond
      (= "app_mention" (:type parsed-message))
      (let [all-replies (bench-f (nlp/reply parsed-message) "getting all replies")
            reply (bench-f (nlp/choose-answer all-replies) "choosing answer")]
        (clojure.pprint/pprint all-replies)
        (println "reply " reply)
        (if (and (not= reply (:message parsed-message)) (not-empty reply))
          (do
            (bench-f (send-post reply) "send post")
            reply)
          (let [rand-sentence (bench-f (clojure.string/join " " (rand-nth @markov/total-sentences)) "getting random reply")]
            (send-post rand-sentence))))
      (= "message" (:type parsed-message))
      (when (and (not (some #{(:user parsed-message)} bot-ids))  (not-empty (:message parsed-message)))
        (println "updating training data")
        (spit "training_data.txt" (str (:message parsed-message) "\n") :append true)
        (bench-f (-> (markov/generate-sentences (:message parsed-message))
                  :new-sentences
                  (lucene/add-sentences)) "add new sentences"))
      (= "disconnect" (:type parsed-message))

      (do
        (println "disconnect event!")
        (reset! socket (get-socket))))))

(comment
  (def choices
    {:choices {:by-name {:rand-word "dave", :answer "weedy :dave: or speedy :dave:"}, :by-verb nil, :by-adj nil, :default nil}})

  (-> (slurp "training_data.txt")
      (markov/generate-sentences))

  (reset! socket (get-socket))

  (handler
   (generate-string
    {:payload
     {:event {:text "dave"
              :user "user"
              :type "app_mention"}}})))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (some #{"generate"} args)
    (do
      (when (.exists (io/file "sentences"))
        (delete-recursively "sentences")
        (delete-recursively "s.txt")
        (.createNewFile (new File "s.txt"))
        (.mkdir (new File "sentences")))
      (-> (slurp "training_data.txt")
          (markov/generate-sentences)
          :sentences
          (lucene/add-sentences)))
    (do
      (-> (slurp "training_data.txt")
          (markov/generate-sentences))
      (reset! socket (get-socket)))))
