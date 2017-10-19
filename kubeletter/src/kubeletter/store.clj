(ns kubeletter.store
  (:require [kubeletter.stores.redis-store :as redis]
            [kubeletter.stores.mem-store :as mem])
  )

(def ^:private chosen-store `mem) ;; default store

(defn decide-store []
  (let [redis-url (System/getenv "REDIS_URL")]
    (if redis-url
      (def chosen-store `redis))))

(defn set-val [key value]
  (`chosen-store/set-val key value))

(defn get-val [key]
  (`chosen-store/get-val key))

(defn tidy [])
