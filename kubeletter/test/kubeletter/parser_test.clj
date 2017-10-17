(ns kubeletter.parser-test
  (:require [clojure.test :refer :all]
            [kubeletter.kubectl :refer :all]
            [kubeletter.parser :refer :all]))

(defn out [result]
  (:out (:proc result)))

(deftest parser-test
  (testing "top node"
    (is (= (type [])
           (type (into-map (out (top-node)))))))
  (testing "top pod"
    (is (= (type [])
           (type (into-map (out (top-pod "production")))))))
  (testing "hpa"
    (is (= (type [])
           (type (into-map (out (hpa "production"))))))))
