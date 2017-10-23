(ns kubeletter.deliver.deliverer
  (:require [kubeletter.deliver.slack :as slack]
            [kubeletter.deliver.console :as console]
            [kubeletter.formatter.formatter :as formatter]
  ))

(def ^:private deliverers {:console #(console/hand-over %)})

(defn registered-deliverers []
  (keys deliverers))

(defn register []
  (if (slack/registerable?)
    (def deliverers
      (-> deliverers (conj {:slack #(slack/hand-over %)})))))

(defn- format-msg [deliverer msg]
  (formatter/cook deliverer msg))

(defn hand-over [msg]
  (->> deliverers
       (map (fn [[deliverer deliver-fn]]
              {deliverer
               (->> msg (format-msg deliverer) deliver-fn)}))))
