(ns kubeletter.harvester.kubectl-test
  (:require [clojure.test :refer :all]
            [kubeletter.harvester.kubectl :refer :all]))

(defn exit-code [result]
  @(:exit-code result))

(deftest success-test
  ;; UNCOMMENT ONLY IF YOUR INTERGRATION TESTING TOOLS PROVIDE HAS RIGHT ENVIRONMENT OR TO TEST LOCALLY
  ;;
  ;; (->> (kubectl "fail")
  ;;      exit-code
  ;;      (= 0)
  ;;      not
  ;;      is
  ;;      (testing "fail"))
  ;; (->> (ping)
  ;;      exit-code
  ;;      (= 0)
  ;;      is
  ;;      (testing "cluster-info"))
  ;; (->> (hpa "production")
  ;;      exit-code
  ;;      (= 0)
  ;;      is
  ;;      (testing "hpa"))
  ;; (->> (top-node)
  ;;      exit-code
  ;;      (= 0)
  ;;      is
  ;;      (testing "top node"))
  ;; (->> (top-pod "production")
  ;;      exit-code
  ;;      (= 0)
  ;;      is
  ;;      (testing "top pod"))
  ;; (->> (get-pod "production")
  ;;      exit-code
  ;;      (= 0)
  ;;      is
  ;;      (testing "get pod production ns"))
  )
