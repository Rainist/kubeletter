(ns kubeletter.schedule
  (:require [hara.io.scheduler :as hs]))

(def ^:private sch nil)

(defn start []
  (def sch (hs/scheduler
            {:print-task
             {:handler (fn [t] (println "task!!"))
              :schedule "/1 * * * * * *"
              }}))

  (hs/start! sch)
  )
