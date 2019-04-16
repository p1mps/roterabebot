(ns roterabebot.output-filter)

(defn message-until-end [coll]
  (take-while #(not (= % "end$")) coll))

(defn not-contains-ends [words]
  (if (empty? words)
    false
    (empty?
     (filter #(= % true)
             (for [w words]
               (= w "end$"))))))
