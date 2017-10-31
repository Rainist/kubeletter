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
       (testing "float round")))
