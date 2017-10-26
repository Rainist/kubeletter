(ns kubeletter.deliver.slack
  (:require [clj-http.client :as req]))

(def ^:private ENV_KEY "DELIVER_SLACK_WEBHOOK_URL")
(def ^:private SLACK_WEBHOOK_URL nil)

(defn- read-url-once []
  (if-not SLACK_WEBHOOK_URL
    (def SLACK_WEBHOOK_URL (System/getenv ENV_KEY))))

(defn registerable? []
  (read-url-once)
  (if (System/getenv ENV_KEY) true false))

(defn- hand-over-by-job [[job result]]
  (if (= job :top-node)
    (let [options {:form-params (or result {})
                   :content-type :json}]
      (-> SLACK_WEBHOOK_URL
          (req/post options)
          (:status)
          (= 200)))))

(defn hand-over [msg]
  (->> msg
       (map hand-over-by-job)
       (reduce #(and %1 %2))))
