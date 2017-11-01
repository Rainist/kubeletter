(ns kubeletter.utils.math-test
  (:require [kubeletter.utils.math :as math]
            [clojure.test :refer :all]))

(deftest roundf-test
  (->> (math/roundf 1.23456)
       (= 1)
       is
       (testing "integer round"))

  (->> (math/roundf 1.23456 3)
       (= (float 1.235))
       is
       (testing "float round"))

  (->> (math/roundf 1.234567890123456 2)
       (= (float 1.23))
       is
       (testing "long tail for float round"))

  (->> (math/roundf 1.0003 2)
       (= 1)
       is
       (testing "integer when possible")))
