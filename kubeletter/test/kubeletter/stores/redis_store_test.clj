(ns kubeletter.stores.redis-store-test
  (:require [kubeletter.stores.redis-store :as redis]
            [clojure.test :refer :all]))

(def ^:private samples
  {:simple-key "test-redis-key"
   :simple-val "test-redis-val"
   :obj-key "test-redis-obj-key"
   :obj-val {:hash {:a 1}
             :vector [:a 1]
             :list '(:a 1)}})

(if (redis/selectable?)
  (let []
    (deftest redis-feature-test
      (->> (redis/ping)
           (= "PONG")
           is
           (testing "redis ping pong"))

      (->> (samples :simple-val)
           (redis/set-val (samples :simple-key))
           (= "OK")
           is
           (testing "redis set val test"))

      (->> (samples :simple-key)
           (redis/get-val)
           (= (samples :simple-val))
           is
           (testing "redis get val test"))

      (->> (samples :obj-val)
           (redis/set-val (samples :obj-key))
           (= "OK")
           is
           (testing "redis set obj val test"))

      (->> (samples :obj-key)
           (redis/get-val)
           (= (samples :obj-val))
           is
           (testing "redis get obj val test"))
      )))
