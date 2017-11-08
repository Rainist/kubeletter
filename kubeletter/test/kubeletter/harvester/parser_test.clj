(ns kubeletter.harvester.parser-test
  (:require [clojure.test :refer :all]
            [kubeletter.harvester.kubectl :refer :all]
            [kubeletter.harvester.parser :refer :all])
  (:use [clojure.pprint]))

(defn- out [result]
  (:out (:proc result)))

(defn- parse-test [result]
  (into-map (out result)))

(deftest parser-test
  ;; DO NOT UNCOMMENT TESTS IN THIS TEST
  ;;
  ;; (testing "top node"
  ;;   (is (= (type [])
  ;;          (type (into-map (out (top-node)))))))
  ;; (testing "top pod"
  ;;   (is (= (type [])
  ;;          (type (into-map (out (top-pod "production")))))))
  ;; (testing "hpa"
  ;;   (is (= (type [])
  ;;          (type (into-map (out (hpa "production")))))))
  )

(deftest parser-raw-test
  (testing "top node (unordered)"
    (is (=
         (parse-test
          {:proc
           {:out
            '("NAME                                               CPU(cores)   CPU%      MEMORY(bytes)   MEMORY%   "
              "ip-172-20-39-187.ap-northeast-1.compute.internal   365m         9%        7480Mi          46%       "
              "ip-172-20-62-138.ap-northeast-1.compute.internal   154m         7%        3827Mi          48%       "
              )}})
         '[{"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal", "CPU(cores)" "365m", "CPU%" "9%",
            "MEMORY(bytes)" "7480Mi", "MEMORY%" "46%"}
           {"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal", "CPU(cores)" "154m", "CPU%" "7%",
            "MEMORY(bytes)" "3827Mi", "MEMORY%" "48%"}]
         )))

  (testing "top pod"
    (is (=
         (parse-test
          {:proc
           {:out
            '("NAME                                                    CPU(cores)   MEMORY(bytes)   "
              "pod-a                                                   18m          303Mi           "
              "pod-b                                                   0m           120Mi           ")}})
         '[{"NAME" "pod-a", "CPU(cores)" "18m", "MEMORY(bytes)" "303Mi"}
           {"NAME" "pod-b", "CPU(cores)" "0m", "MEMORY(bytes)" "120Mi"}]
         )))

  (testing "hpa"
    (is (=
         (parse-test
          {:proc
           {:out
            '("NAME                                   REFERENCE                                         TARGETS      MINPODS   MAXPODS   REPLICAS   AGE"
              "deploy-a                               Deployment/deploy-a                               17% / 50%    3         100       3          46d"
              "deploy-b                               Deployment/deploy-b                               20% / 100%   4         4         4          46d"
              )}}
          )
         '[{"NAME" "deploy-a", "REFERENCE" "Deployment/deploy-a", "TARGETS" "17% / 50%",
            "MINPODS" "3", "MAXPODS" "100", "REPLICAS" "3", "AGE" "46d"}
           {"NAME" "deploy-b", "REFERENCE" "Deployment/deploy-b", "TARGETS" "20% / 100%",
            "MINPODS" "4", "MAXPODS" "4", "REPLICAS" "4", "AGE" "46d"}]
         )))

  (->> {:proc
        {:out
         '("NAME                                                   READY     STATUS              RESTARTS   AGE   "
           "pod-a                                                  1/1       Running             0          1d    "
           "pod-b                                                  2/2       Running             5          2d    "
           "pod-c                                                  2/2       Running             11         3d    ")}}
       parse-test
       (= [{"NAME" "pod-a", "READY" "1/1", "STATUS" "Running", "RESTARTS" "0", "AGE" "1d"}
           {"NAME" "pod-b", "READY" "2/2", "STATUS" "Running", "RESTARTS" "5", "AGE" "2d"}
           {"NAME" "pod-c", "READY" "2/2", "STATUS" "Running", "RESTARTS" "11", "AGE" "3d"}])
       is
       (testing "get pod"))
  )
