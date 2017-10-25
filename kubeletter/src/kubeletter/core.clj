(ns kubeletter.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [kubeletter.schedule :as sch]
            [kubeletter.deliver.deliverer :as deliverer]
            [kubeletter.tasks :as tasks]
            [kubeletter.store :as store]
            ))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "hello World"})

(defn -main [& args]
  (tasks/fetch-namespaces)
  (deliverer/register)
  (store/decide-store)
  (sch/start)
  (jetty/run-jetty handler {:port 3000}))
