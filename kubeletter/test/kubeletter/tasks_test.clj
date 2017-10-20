(ns kubeletter.tasks-test
  (:require [clojure.test :refer :all]
            [kubeletter.tasks :refer :all]))

(def ^:private parse-namespaces #'kubeletter.tasks/parse-namespaces)

(deftest namspaces-test
  (->> (parse-namespaces "")
       (= '())
       is
       (testing "parse namespaces: \"\""))

  (->> (parse-namespaces nil)
       (= '())
       is
       (testing "parse namespaces nil")))
