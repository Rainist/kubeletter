(ns kubeletter.reporter
  (:use [clojure.pprint]))

(defn report [ready-report]
  (-> ready-report
      pprint))
