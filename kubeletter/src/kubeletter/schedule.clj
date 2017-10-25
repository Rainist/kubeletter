(ns kubeletter.schedule
  (:require [hara.io.scheduler :as hs]
            [kubeletter.tasks :as tasks]
            [clj-time.coerce :as c]
            ))

(defn- simulation? [t params sch-name]
  (let [collect-time-fn (params :collect-time-fn)]
    (if collect-time-fn (collect-time-fn t)))
  (if (params :simulation)
    (if-not (params :simulation-quite)
      (println sch-name " schedule simulation! - " t)))
  (= true (params :simulation)))

(defn schedules []
  (hs/scheduler
   {;; :test-task
    ;; {:handler (fn [t] (println "test task! every 1 sec"))
    ;;  :schedule "/1 * * * * * *"}
    :report-every-hour
    {:handler
     (fn [t params]
       (if-not (simulation? t params :report-every-hour)
         (-> t c/from-date tasks/report-status)))
     :schedule "0 0 /1 * * * *"}
    }))
    ;; :report-every-day
    ;; {:handler (fn [t params] (println "every day task! " t params))
    ;;  :schedule "0 0 1 * * * *"}
    ;; }))

(defn start []
  (println "Schedule starting..")
  (hs/start! (schedules))
  (println "Schedule has started!"))
