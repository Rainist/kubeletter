(ns kubeletter.reporter
  (:require [kubeletter.deliver.deliverer :as deliverer])
  (:use [clojure.pprint]))

(defn report [ready-report]
  (-> ready-report
      deliverer/hand-over))
