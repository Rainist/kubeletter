(ns kubeletter.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [kubeletter.schedule :as sch]
            [kubeletter.kubectl :as kctl]
            ))


(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "hello World"})

(defn -main [& args]
  ;; (sch/start)
  (jetty/run-jetty handler {:port 3000}))
