(ns kubeletter.tasks-test
  (:require [clojure.test :refer :all]
            [kubeletter.tasks :refer :all]))

(def ^:private parse-namespaces #'kubeletter.tasks/parse-namespaces)
(def ^:private keyword-with-ns #'kubeletter.tasks/keyword-with-ns)

(deftest namspaces-test
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
