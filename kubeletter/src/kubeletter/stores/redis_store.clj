(ns kubeletter.stores.redis-store
  (:require [taoensso.carmine :as car])
  )

(def ^:private ENV_KEY "REDIS_HOST")
(def ^:private REDIS_URL nil)
(def ^:private server1-conn nil)

(defn- read-host-once []
  (if (and (not REDIS_URL) (System/getenv ENV_KEY))
    (def REDIS_URL (str "redis://" (System/getenv ENV_KEY)))))

(defn- prepare-conn-once []
  (if REDIS_URL
    (def server1-conn
      {:pool {}
       :spec {:uri REDIS_URL}})))

(defn selectable? []
  (read-host-once)
  (prepare-conn-once)
  (if (System/getenv ENV_KEY) true false))

(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn ping []
  (wcar* (car/ping)))

(defn set-val [key value]
  (wcar*
   (car/set key value)))

(defn get-val [key]
  (wcar*
   (car/get key)))

(defn tidy []
  ;;TODO: tidy old data
  )
