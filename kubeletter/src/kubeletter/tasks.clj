(ns kubeletter.tasks
  (:require
   [kubeletter.kubectl :as kubectl :refer [top-node top-pod hpa]]
   [kubeletter.kube-parser :as kube-parser :refer [parse-kube]]
   [kubeletter.analyzer :as analyzer :refer [analyze]]
   [kubeletter.store :as store :refer [set-val]]
   [kubeletter.reporter :as reporter :refer [report]]
   ))

(defn- job-kube [job-keyword kube-job timestamp]
  (let [store-key (str timestamp "-" (name job-keyword))
        kube-result (parse-kube kube-job)]
    (set-val store-key kube-result)))

(defn- job-top-node [timestamp] (job-kube :top-node (top-node) timestamp))
(defn- job-top-pod-prod [timestamp] (job-kube :top-pod-prod (top-pod "production") timestamp))
(defn- job-top-pod-dev [timestamp] (job-kube :top-pod-dev (top-pod "development") timestamp))
(defn- job-hpa-prod [timestamp] (job-kube :hpa-prod (hpa "production") timestamp))
(defn- job-hpa-dev [timestamp] (job-kube :hpa-dev (hpa "development") timestamp))

;; [FLOW] kubectl -> parse -> save -> analyze -> report
(defn report-status []
  (let [timestamp "TEST_TIMESTAMP"]
    (job-top-node timestamp)
    ;; (job-top-pod-prod timestamp)
    ;; (job-top-pod-dev timestamp)
    ;; (job-hpa-prod timestamp)
    ;; (job-hpa-dev timestamp)
    (report (analyze timestamp))))
