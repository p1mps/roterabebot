(ns roterabebot.core
  (:require
   [clj-http.client :as client]
   [roterabebot.nlp :as nlp]
   [gniazdo.core :as ws]
   [cheshire.core :refer :all]
   [roterabebot.markov :as markov]
   [roterabebot.lucene :as lucene])
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

(declare get-socket)

(defn on-connect [_]
  (println "Connected to WebSocket."))

(defn on-close [code reason]
  (println "Connection to WebSocket closed.\n"
           (format "[%s] %s" code reason))
  (reset! socket (get-socket)))

(defn on-error [e]
  (println "ERROR:" e)
  (ws/close @socket)
  (reset! socket (get-socket)))

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


(defn choose-answer [{:keys [choices]}]
  (->> (select-keys choices [:by-name :by-verb :by-adj :default])
       (vals)
       (map :answer)
       (filter not-empty)
       (first)))


(defn handler [message]
  (let [parsed-message (get-message (parse-string message true))]
    (ws/send-msg @socket message)
    (println parsed-message)
    (cond
      (= "app_mention" (:type parsed-message))
      (let [all-replies (nlp/reply parsed-message)
            reply (choose-answer all-replies)]
        (clojure.pprint/pprint all-replies)
        (println "reply " reply)
        (if (not-empty reply)
          (do
            (send-post reply)
            reply)
          (let [rand-sentence (rand-nth sentences)]
            (println "senting random sentence!")
            (send-post rand-sentence))))
      (= "message" (:type parsed-message))
      (when (not= (:user parsed-message) bot-id)
        (spit "training_data.txt" (:message parsed-message) :append true))

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
  (if (some #{"generate"} args)
    (-> (markov/generate-sentences (slurp "training_data.txt"))
        (lucene/add-sentences))
    (reset! socket (get-socket)))
  )
