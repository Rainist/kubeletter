(ns kubeletter.harvester.kubectl
  (:require [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [clojure.string :as str]))

(def ^:private IS_INSIDE_K8S false)
(def ^:private K8S-SVC-HOST nil)
(def ^:private K8S-SVC-PORT nil)

(defn detect-k8s-once []
  (if-not IS_INSIDE_K8S
    (if (System/getenv "RUNNING_INSIDE_K8S")
      (def IS_INSIDE_K8S true))))

(detect-k8s-once)

(defn fetch-k8s-info-once []
  (if-not IS_INSIDE_K8S
    (do
      (def K8S-SVC-HOST (System/getenv "KUBERNETES_SERVICE_HOST"))
      (def K8S-SVC-PORT (System/getenv "KUBERNETES_SERVICE_PORT")))))

(fetch-k8s-info-once)

(defn- kube-args [args]
  (-> (if IS_INSIDE_K8S []
          [(str "--server=http://" K8S-SVC-HOST ":" K8S-SVC-PORT)])
      (concat args [{:seq true, :verbose true, :throw false}])
      vec
      ))

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

(defn get-pod
  ([] (get-obj "pod"))
  ([namespace]
   (get-obj namespace "pod")))
