(ns kubeletter.deliver.deliverer-test
  (:require [clojure.test :refer :all]
            [kubeletter.deliver.deliverer :refer :all]))

(deftest deliverer-test
  (testing "hand-over"
    (register)
    (is (=
         (map (fn [key] {key true}) (registered-deliverers)) ;; ex) '({:slack true})
         (hand-over "msg")))))
