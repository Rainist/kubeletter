(ns kubeletter.kubectl
  (:require [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [clojure.string :as str]))

(defn- kube-args [args]
  (vec
   (concat
    [(str
      "--server=http://"
      (System/getenv "KUBERNETES_SERVICE_HOST")
      ":"
      (System/getenv "KUBERNETES_SERVICE_PORT")
      )]
    args
    [{:seq true, :verbose true, :throw false}])))

(defn- str-to-args [str]
  (str/split str #" "))

(defn kubectl [arg-str]
  (let-programs [law-kubectl "/usr/local/bin/kubectl"]
    (apply
     law-kubectl
     (kube-args (str-to-args arg-str)))))

(defn ping []
  (kubectl "cluster-info"))

(defn get-obj
  ([object]
   (kubectl (str "get " object)))
  ([namespace, object]
   (kubectl (str "get -n " namespace " " object))))

(defn top
  ([object]
   (kubectl (str "top " object))
   )
  ([namespace, object]
   (kubectl (str "top -n " namespace " " object))
   ))

(defn hpa [namespace]
  (get-obj namespace, "hpa"))

(defn top-node []
  (top "node"))

(defn top-pod [namespace]
  (top namespace "pod"))
