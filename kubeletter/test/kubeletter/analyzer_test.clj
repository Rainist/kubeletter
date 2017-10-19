(ns kubeletter.analyzer-test
  (:require [clojure.data :as data :refer [diff]])
  (:require [clojure.test :refer :all]
            [kubeletter.analyzer :refer :all])
  (:use [clojure.pprint]))

(def
  ^:private
  top-node-samples
  {:simple
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (154 "m"),
      "CPU%" (7 "%"),
      "MEMORY(bytes)" (3827 "Mi"),
      "MEMORY%" (48 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (365 "m"),
      "CPU%" (9 "%"),
      "MEMORY(bytes)" (7480 "Mi"),
      "MEMORY%" (46 "%")}],
   :simple-diff-num
    '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
       "CPU(cores)" (144 "m"),
       "CPU%" (10 "%"),
       "MEMORY(bytes)" (3227 "Mi"),
       "MEMORY%" (78 "%")}
      {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
       "CPU(cores)" (565 "m"),
       "CPU%" (2 "%"),
       "MEMORY(bytes)" (7980 "Mi"),
       "MEMORY%" (56 "%")}],
   :simple-same-diffed
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (0 "m"),
      "CPU%" (0 "%"),
      "MEMORY(bytes)" (0 "Mi"),
      "MEMORY%" (0 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (0 "m"),
      "CPU%" (0 "%"),
      "MEMORY(bytes)" (0 "Mi"),
      "MEMORY%" (0 "%")}],
   :simple-diff-num-diffed
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (-10 "m"),
      "CPU%" (3 "%"),
      "MEMORY(bytes)" (-600 "Mi"),
      "MEMORY%" (30 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (200 "m"),
      "CPU%" (-7 "%"),
      "MEMORY(bytes)" (500 "Mi"),
      "MEMORY%" (10 "%")}],
   })

(def
  ^:private
  top-node-results
  {:simple-same
   {:terminated [],
    :added [],
    :existed (map top-node-samples [:simple :simple-same-diffed])},
   :simple-diff-num-diffed
   {:terminated [],
    :added [],
    :existed (map top-node-samples [:simple-diff-num :simple-diff-num-diffed])},
   })

(deftest compare-top-node-test
  (->> (compare-kube
        (:simple top-node-samples)
        (:simple top-node-samples))
       (= (:simple-same top-node-results))
       is
       (testing "same"))

  (->> (compare-kube
        (:simple top-node-samples)
        (:simple-diff-num top-node-samples))
       (= (:simple-diff-num-diffed top-node-results))
       is
       (testing "different number")))

(deftest analyze-test
  (-> (analyze "MOCK_TIMESTAMP")
       (contains? :top-node)
       (testing "analyze")))

