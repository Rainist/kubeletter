(ns kubeletter.store
  (:require [kubeletter.stores.redis-store :as redis]
            [kubeletter.stores.mem-store :as mem])
  )

(def ^:private chosen-store 'mem) ;; default store

(defn decide-store []
  (if (redis/selectable?)
    (def chosen-store 'redis)))

(defn- store-symbol [fn-symbol]
  (symbol (name chosen-store) (name fn-symbol)))

(defn- local-sym-resolve [lc-symbol]
  (ns-resolve 'kubeletter.store lc-symbol))

(defn- delegate [fn-symbol args]
  (-> (store-symbol fn-symbol)
      local-sym-resolve
      (apply args)))

(defn set-val [& args]
  (delegate 'set-val args))

(defn get-val [& args]
  (delegate 'get-val args))

(defn debug [& args]
  (delegate 'debug args))

(defn tidy [& args]
  (delegate 'tidy args))
