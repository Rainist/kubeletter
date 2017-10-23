(ns kubeletter.tasks-test
  (:use [clojure.pprint])
  (:require [clojure.test :refer :all]
            [clj-time.coerce :as c]
            [kubeletter.tasks :refer :all]))

(def ^:private parse-namespaces #'kubeletter.tasks/parse-namespaces)
(def ^:private keyword-with-ns #'kubeletter.tasks/keyword-with-ns)

(deftest report-status-test
  ;;DO NOT UNCOMMENT
  ;; (-> #inst "2017-10-19T00:00:00.00-00:00"
  ;;     c/from-date
  ;;     report-status pprint)
  )

(deftest namspaces-test
  (->> (parse-namespaces "prod,dev")
       (= '("prod" "dev"))
       is
       (testing "parse namespaces: \"\""))

  (->> (parse-namespaces "")
       (= '())
       is
       (testing "parse namespaces: \"\""))

  (->> (parse-namespaces nil)
       (= '())
       is
       (testing "parse namespaces nil")))

(deftest keyword-with-ns-test
  (->> (keyword-with-ns :job-keyword "kube-namespace")
       (= :job-keyword#kube-namespace)))
