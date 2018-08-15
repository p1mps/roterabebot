(ns roterabebot.core
  (:gen-class))

(require '[roterabebot.clack :as clack])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (clack/start-chat))
