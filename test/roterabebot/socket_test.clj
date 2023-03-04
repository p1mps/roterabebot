(ns roterabebot.socket-test
  (:require
   [clojure.core.reducers :as r]
   [clojure.reflect :as reflect]
   [clojure.test :as t]
   [gniazdo.core :as ws]
   [mount.core :as mount :refer [defstate]]
   [org.httpkit.server :as server]
   [roterabebot.http :as http]
   [roterabebot.socket :as sut])
  (:import
   (java.util.concurrent Future)
   (org.eclipse.jetty.websocket.api Session)))

(declare ^:dynamic *recv*)

(declare ws-srv)


(defstate ^{:on-reload :noop} server
  :start (server/run-server ws-srv {:port 65430})
  :stop (server))

(defn- ws-srv
  [req]
  (server/with-channel req conn
    (server/on-receive conn (partial *recv* req conn))
    (server/on-close conn (partial identity))))

(t/use-fixtures
  :each
  (fn [f]
    (try
      (mount/start #'server)
      (f)
      (catch Exception e
        (println e))
      (finally
        (mount/stop #'sut/ws-socket)
        (mount/stop #'server)
        ))))



(def ^:private url "ws://localhost:65430/")


(defmacro ^:private with-timeout
  [& body]
  `(let [f# (future ~@body)]
     (try
       (.get ^Future f# 1 java.util.concurrent.TimeUnit/SECONDS)
       (finally
         (future-cancel f#)))))

(def message (atom nil))
(def handler-sem (java.util.concurrent.Semaphore. 0))
(def close (atom nil))
(def close-sem (java.util.concurrent.Semaphore. 0))

(t/deftest on-receive
  (t/testing "on receive works"
    (with-redefs [*recv* (fn [_ conn msg]
                           (server/send! conn msg))
                  http/ws-url (constantly url)]
      (mount/start-with-args {:handler-fn (fn [payload]
                                            (reset! message payload)
                                            (.release handler-sem))
                              :on-close-fn (fn [_ _]
                                             (reset! close "close")
                                             (.release close-sem))}
                             #'sut/ws-socket)
      (ws/send-msg sut/ws-socket "hello")
      (with-timeout (.acquire handler-sem))
      (t/is (= "hello" @message))
      (mount/stop #'server)
      (with-timeout (.acquire close-sem))
      (t/is (= "close" @close))
      )))
