(ns kubeletter.formatter.console
  (:require [clojure.string :refer [join]]))

(defn- cook-str [result]
  (-> result str))

(defn- cook-top-node-terminated [result]
  (->> (->> result (map (fn [row] (row "NAME"))))
       (join "\n")
       (str "Terminated:\n")))

(defn- titles-text [result]
  (->> result
       (map #(->> % keys (join "\t")))
       (join "")))

(defn- top-node-map [rows]
  (->> rows
       (map (fn [row] {(row "NAME") row}))
       (apply merge)))

(defn- merge-curr-comp [curr comp]
  (->> curr
       (map (fn [[k v]]
              {k (if (= k "NAME") v
                     (str v (comp k)))}))
       (apply merge)))

(defn- cook-top-node-existed [result]
  (let [curr (top-node-map (first result))
        comp (top-node-map (last result))]
    (->> curr
         (map (fn [[k row]] (merge-curr-comp row (comp k))))
         (map #(->> % vals (join "\t")))
         (join "\n")
         (str "Existed:\n"
              (->> (first result) (take 1) titles-text)
              "\n"))))

(defn- cook-top-node-added [result]
  (->> (->> result (map #(->> % vals (join "\t"))))
       (join "\n")
       (str "added:\n" (->> result (take 1) titles-text) "\n")))

(defn- cook-top-node [result]
  (->> (list
        (->> :existed
             (get result)
             cook-top-node-existed)
        (->> :terminated
             (get result)
             cook-top-node-terminated)
        (->> :added
             (get result)
             cook-top-node-added))
       (join "\n\n")))

(defn- cook-by-job [job-id result]
  {job-id
   (case job-id
     :top-node (cook-top-node result)
     :cook-str (cook-str result))})

(defn cook [msg]
  (->> msg
       (map (fn [[job-id job-result]] (cook-by-job job-id job-result)))))
