(ns roterabebot.socket
  (:require
   [gniazdo.core :as ws]
   [mount.core :as mount :refer [defstate]]
   [roterabebot.http :as http]))

(declare get-socket)

(defstate ^{:on-reload :noop} ws-socket
  :start (get-socket (merge
                      {:url (http/ws-url)}
                      (mount/args)))
    :stop  (ws/close ws-socket))


(defn on-close [status reason]
  (println "socket closing" status reason)
  (mount/stop #'ws-socket)
  (mount/start #'ws-socket))


(defn on-receive [message sentences handler-fn]
  (println "received message" message)
  ;; we're alive
  (ws/send-msg ws-socket message)
  ;; sending back a message
  (handler-fn message sentences))


(defn get-socket [{:keys [url handler-fn on-close-fn sentences]}]
  (ws/connect
      url
      :on-receive (fn [message]
                    (on-receive message sentences handler-fn))
      :on-connect #(println "connected" %)
      :on-close (fn [status reason]
                  (on-close-fn status reason))))





(comment
  (mount/start #'ws-socket)
  (mount/stop #'ws-socket))
