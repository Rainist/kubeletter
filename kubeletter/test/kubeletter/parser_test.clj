(ns kubeletter.parser-test
  (:require [clojure.test :refer :all]
            [kubeletter.kubectl :refer :all]
            [kubeletter.parser :refer :all]))

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
  (testing "top node"
    (is (=
         (parse-test
          {:proc
           {:out
            '("NAME                                               CPU(cores)   CPU%      MEMORY(bytes)   MEMORY%   "
              "ip-172-20-62-138.ap-northeast-1.compute.internal   154m         7%        3827Mi          48%       "
              "ip-172-20-39-187.ap-northeast-1.compute.internal   365m         9%        7480Mi          46%       "
              )}})
         '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
            "CPU(cores)" "154m",
            "CPU%" "7%",
            "MEMORY(bytes)" "3827Mi",
            "MEMORY%" "48%"}
           {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
            "CPU(cores)" "365m",
            "CPU%" "9%",
            "MEMORY(bytes)" "7480Mi",
            "MEMORY%" "46%"}]
         )))

  (testing "top pod"
    (is (=
         (parse-test
          {:proc
           {:out
            '("NAME                                                    CPU(cores)   MEMORY(bytes)   "
              "simon-3770191713-xgpww                                  18m          303Mi           "
              "banksalad-certificate-web-2605397671-ktb92              0m           120Mi           "
              )}}
          )
         '[{"NAME" "simon-3770191713-xgpww",
            "CPU(cores)" "18m",
            "MEMORY(bytes)" "303Mi"}
           {"NAME" "banksalad-certificate-web-2605397671-ktb92",
            "CPU(cores)" "0m",
            "MEMORY(bytes)" "120Mi"}]
         )))

  (testing "hpa"
    (is (=
         (parse-test
          {:proc
           {:out
            '("NAME                                   REFERENCE                                         TARGETS      MINPODS   MAXPODS   REPLICAS   AGE"
              "alfred                                 Deployment/alfred                                 17% / 50%    3         100       3          46d"
              "alfred-legacy                          Deployment/alfred-legacy                          20% / 100%   4         4         4          46d"
              )}}
          )
         '[{"NAME" "alfred",
            "REFERENCE" "Deployment/alfred",
            "TARGETS" "17% / 50%",
            "MINPODS" "3",
            "MAXPODS" "100",
            "REPLICAS" "3",
            "AGE" "46d"}
           {"NAME" "alfred-legacy",
            "REFERENCE" "Deployment/alfred-legacy",
            "TARGETS" "20% / 100%",
            "MINPODS" "4",
            "MAXPODS" "4",
            "REPLICAS" "4",
            "AGE" "46d"}]
         )))
  )
