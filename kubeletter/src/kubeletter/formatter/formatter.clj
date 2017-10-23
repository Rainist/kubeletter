(ns kubeletter.formatter.formatter
  (:require [kubeletter.formatter.console :as console]
            [kubeletter.formatter.slack :as slack]
            ))

(def ^:private formatters {:slack #(slack/cook %) :console #(console/cook %)})

(defn cook [formatter msg]
  (-> formatter
      formatters
      (apply [msg])))
