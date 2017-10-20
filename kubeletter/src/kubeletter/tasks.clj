(ns kubeletter.tasks
  (:require
   [clojure.string :as str]
   [kubeletter.harvester.kubectl :as kubectl :refer [top-node top-pod hpa]]
   [kubeletter.harvester.kube-parser :as kube-parser :refer [parse-kube]]
   [kubeletter.analyzer :as analyzer :refer [analyze]]
   [kubeletter.store :as store :refer [set-val]]
   [kubeletter.reporter :as reporter :refer [report]]
   ))

(defn- store-key [job-keyword timestamp]
  (str timestamp "-" (name job-keyword)))

(defn- commit-kube-job [job-keyword kube-job timestamp]
  (set-val
   (store-key job-keyword timestamp)
   (parse-kube kube-job)))

(defn- job-top-node [timestamp] (commit-kube-job :top-node (top-node) timestamp))
(defn- job-top-pod [timestamp namespace] (commit-kube-job :top-pod-prod (top-pod namespace) timestamp))
(defn- job-hpa [timestamp namespace] (commit-kube-job :hpa-prod (hpa namespace) timestamp))

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
(defn report-status []
  (let [timestamp "TEST_TIMESTAMP"]
    (job-top-node timestamp)
    (->> given-namespaces
         (map #(perform-ns-jobs timestamp %)))
    (report (analyze timestamp))))
