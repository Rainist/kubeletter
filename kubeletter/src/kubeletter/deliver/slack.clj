(ns kubeletter.deliver.slack
  )

(defn registerable? []
  (if (System/getenv "DELIVER_SLACK_WEBHOOK_URL") true false))

(defn hand-over [msg]
  (println "pretend delivering")
  true
  )
