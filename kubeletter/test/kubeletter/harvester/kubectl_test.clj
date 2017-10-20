(ns kubeletter.harvester.kubectl-test
  (:require [clojure.test :refer :all]
            [kubeletter.harvester.kubectl :refer :all]))

(defn exit-code [result]
  @(:exit-code result))

(deftest success-test
  ;; UNCOMMENT ONLY IF YOUR INTERGRATION TESTING TOOLS PROVIDE HAS RIGHT ENVIRONMENT OR TO TEST LOCALLY
  ;;
  ;; (testing "fail"
  ;;   (is (not (= 0
  ;;          (exit-code (kubectl "fail"))))))
  ;; (testing "cluster-info"
  ;;   (is (= 0
  ;;          (exit-code (ping)))))
  ;; (testing "hpa"
  ;;   (is (= 0
  ;;          (exit-code (hpa "production")))))
  ;; (testing "top node"
  ;;   (is (= 0
  ;;          (exit-code (top-node)))))
  ;; (testing "top pod"
  ;;   (is (= 0
  ;;          (exit-code (top-pod "production")))))
  )
