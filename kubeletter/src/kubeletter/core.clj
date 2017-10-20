(ns kubeletter.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [kubeletter.schedule :as sch]
            [kubeletter.deliver.deliverer :as deliverer]
            [kubeletter.tasks :as tasks]
            ))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "hello World"})

(defn -main [& args]
  ;; (sch/start)
  (tasks/fetch-namespaces)
  (deliverer/register)
  (jetty/run-jetty handler {:port 3000}))
