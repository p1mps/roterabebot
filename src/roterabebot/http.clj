(ns roterabebot.http
  (:require
   [cheshire.core :as json]
   [clj-http.client :as client]))


(def ws-token (slurp "ws-token.txt"))
(def api-token (slurp "api-token.txt"))
(def channel "CER5J71LY")
(def channel-test "C0291CCA79A")



(defn send-message [text]
  (client/post "https://slack.com/api/chat.postMessage"
               {:headers      {"Content-type"  "application/json"
                               "Authorization" api-token}
                :form-params  {:channel channel-test :text text}
                :content-type :json}))


(defn ws-url []
  (-> (client/post  "https://slack.com/api/apps.connections.open"
                    {:headers {"Content-type"  "application/json;charset=UTF-8"
                               "Authorization" (str "Bearer " ws-token)}})
      :body
      (json/parse-string true)
      :url))
