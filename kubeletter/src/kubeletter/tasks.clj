(ns kubeletter.tasks
  (:require
   [clojure.string :as str]
   [kubeletter.harvester.kubectl :as kubectl :refer [top-node top-pod hpa]]
   [kubeletter.harvester.kube-parser :as kube-parser :refer [parse-kube]]
   [kubeletter.analyzer :as analyzer :refer [analyze kube-key]]
   [kubeletter.store :as store :refer [set-val debug]]
   [kubeletter.reporter :as reporter :refer [report]]
   ))

(defn- store-key [job-keyword timestamp]
  (kube-key job-keyword timestamp))

(defn- commit-kube-job [job-keyword kube-job timestamp]
  (set-val
   (store-key job-keyword timestamp)
   (parse-kube kube-job)))

(defn- keyword-with-ns [kw ns]
  (-> kw name (str "#" ns) keyword))

(defn- job-top-node [timestamp] (commit-kube-job :top-node (top-node) timestamp))

(defn- job-top-pod [timestamp namespace]
  (-> (keyword-with-ns :top-pod namespace)
      (commit-kube-job (top-pod namespace) timestamp)))

(defn- job-hpa [timestamp namespace]
  (-> (keyword-with-ns :hpa namespace)
      (commit-kube-job (hpa namespace) timestamp)))

(def ^:private given-namespaces [])

(defn- parse-namespaces [text]
  (->> (str/split (or text "") #",")
       (filter #(-> (count %)
                    (not= 0)))))

(defn fetch-namespaces []
  (->> (System/getenv "KUBE_NAMESPACES")
       parse-namespaces
       (def given-namespaces)))

(defn- perform-ns-jobs [timestamp namespace]
  (->> '(job-top-pod job-hpa)
       (map #(% timestamp namespace))))

;; [FLOW] kubectl -> parse -> save -> analyze -> report
(defn report-status [t]
  (let [timestamp t]
    (job-top-node timestamp)
    (->> given-namespaces
         (map #(perform-ns-jobs timestamp %)))
    (-> (analyze timestamp)
        report)))
