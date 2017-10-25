(ns kubeletter.formatter.console-test
  (:require [clojure.test :refer :all]
            [kubeletter.formatter.console :refer :all])
  (:use [clojure.pprint]))

(def
  ^:private
  top-node-samples
  {:terminated
   '[{"NAME" "ip-172-20-10-13.ap-northeast-1.compute.internal",
      "CPU(cores)" (154 "m"),
      "CPU%" (7 "%"),
      "MEMORY(bytes)" (3827 "Mi"),
      "MEMORY%" (48 "%")}]
   :added
   '[{"NAME" "ip-172-20-12-18.ap-northeast-1.compute.internal",
      "CPU(cores)" (365 "m"),
      "CPU%" (9 "%"),
      "MEMORY(bytes)" (7480 "Mi"),
      "MEMORY%" (46 "%")}],
   :existed-curr
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
   :existed-comp
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
  {:top-node-sample
   {:terminated (top-node-samples :terminated),
    :added (top-node-samples :added),
    :existed (map top-node-samples [:existed-curr :existed-comp])},
   })

(def ^:private top-node-expected
  '"Existed:
NAME	CPU(cores)	CPU%	MEMORY(bytes)	MEMORY%
ip-172-20-62-138.ap-northeast-1.compute.internal	(154 \"m\")(-10 \"m\")	(7 \"%\")(3 \"%\")	(3827 \"Mi\")(-600 \"Mi\")	(48 \"%\")(30 \"%\")
ip-172-20-39-187.ap-northeast-1.compute.internal	(365 \"m\")(200 \"m\")	(9 \"%\")(-7 \"%\")	(7480 \"Mi\")(500 \"Mi\")	(46 \"%\")(10 \"%\")

Terminated:
ip-172-20-10-13.ap-northeast-1.compute.internal

added:
NAME	CPU(cores)	CPU%	MEMORY(bytes)	MEMORY%
ip-172-20-12-18.ap-northeast-1.compute.internal	(365 \"m\")	(9 \"%\")	(7480 \"Mi\")	(46 \"%\")")

(->> (-> {:top-node (top-node-results :top-node-sample)}
         cook
         (:top-node))
     (= top-node-expected)
     is
     (testing "format for console test")
     (deftest format-for-console-test))

