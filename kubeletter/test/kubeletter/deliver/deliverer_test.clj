(ns kubeletter.deliver.deliverer-test
  (:require [clojure.test :refer :all]
            [kubeletter.deliver.slack :as slack]
            [kubeletter.deliver.deliverer :refer :all]))

(deftest deliverer-test
  (->> (hand-over {:top-node
                   {:added []
                    :terminated []
                    :existed [[] []]}})
       (= (->> (registered-deliverers)
               (map (fn [key] {key true}))
               (apply merge)
               vals
               (reduce #(= %1 %2 true))))
       is
       (testing "hand-over")))
