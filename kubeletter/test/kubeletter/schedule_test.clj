(ns kubeletter.schedule-test
  (:require [clojure.test :refer :all]
            [hara.io.scheduler :as hs]
            [kubeletter.schedule :refer :all])
  (:use [clojure.pprint]))

(deftest report-with-simulated-schedule
  ;; DO NOT UNCOMMENT THIS TEST
  ;; (def ^:private T1 #inst "2017-10-19T00:00:00.00-00:00")
  ;; (def ^:private T2 #inst "2017-10-19T02:00:01.00-00:00")

  ;; (hs/simulate
  ;;  (schedules)
  ;;  {:start T1
  ;;   :end T2
  ;;   :step 1800
  ;;   :pause 5000
  ;;   })
  )

(def ^:private simulation? #'kubeletter.schedule/simulation?)
(defn- sim? [params]
  (simulation?
   #inst "2017-10-19T00:00:00.00-00:00"
   params :test-sch))

(deftest simulation?-test
  (->> (sim? {:simulation true})
       (= true)
       is
       (testing "simulation true => true"))
  (->> (sim? {:simulation false})
       (= false)
       is
       (testing "simulation false => false"))
  (->> (sim? {:simulation nil})
       (= false)
       is
       (testing "simulation nil => false"))
  (->> (sim? {})
       (= false)
       is
       (testing "simulation empty => false"))
  )

(deftest simluate-test
  (def ^:private call-times 0)
  (def ^:private test-schedules
    (let [sch (schedules)]
      (-> sch
          (hs/reparametise-task
           :report-every-hour
           {:simulation true
            :simulation-quite true
            :collect-time-fn
            (fn [t]
              (def call-times (inc call-times)))}
           ))
      sch))

  (def ^:private T1 #inst "2017-10-19T00:00:00.00-00:00")
  (def ^:private T2 #inst "2017-10-20T01:00:00.00-00:00")

  (testing "simluate schedule"
    (hs/simulate
     test-schedules
     {:start T1
      :end T2
      :step 1800 ;; 30 minutes step instead of every second
      })

    (is (= call-times 25))))

