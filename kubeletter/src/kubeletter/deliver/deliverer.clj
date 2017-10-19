(ns kubeletter.deliver.deliverer
  (:require [kubeletter.deliver.slack :as slack])
  )

(def ^:private deliverers {})

(defn registered-deliverers []
  (keys deliverers))

(defn register []
  (let [slack-webhook-url (System/getenv "DELIVER_SLACK_WEBHOOK_URL")]
    (if slack-webhook-url
      (def
        deliverers
        (conj
         deliverers
         {:slack
          (fn [msg]
            (slack/hand-over msg)
            )})))))

(defn hand-over [msg]
  (map
   (fn [[deliverer deliver-fn]]
     {deliverer (deliver-fn msg)})
   (into [] deliverers)))
