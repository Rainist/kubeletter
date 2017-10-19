(ns kubeletter.analyzer
  (:require [clojure.data :as data :refer [diff]])
  )

;;TODO: divide this module into
;; 1. specific kube-analyzer
;; 2. command analyzer
;; so that you can extend easily with other type of analyzers

(defn- only-names [kube-result]
  (->> (->> kube-result
            (map (fn [row] (row "NAME"))))
       (into #{})))

(defn- diff-kube-names [past fresh]
  (diff
   (only-names past)
   (only-names fresh)
   ))

(defn- filter-by-names [names kube-result]
  (->> kube-result
       (filter
        (fn [row] (some #(= (row "NAME") %) names)))
       vec))

(declare subtract-if-num-kv)

(defn- subtract-if-num [past fresh]
  (let [kind (type past)]
    (cond
      (= kind (type {}))
      (map subtract-if-num-kv past fresh),

      (some #(= kind %) (map type [[] '() '(0)]))
      (map subtract-if-num past fresh),

      (some #(= kind %) (map type [""]))
      (if (compare past fresh)
        fresh
        past
        ),

      (some #(= kind %) (map type [0]))
      (- fresh past),

      :else
      9999
      )))

(defn- subtract-if-num-kv [past fresh]
  (->> {(first fresh)
        (->> (mapv last [past fresh])
             (apply subtract-if-num))}
       (cond
         (->> (mapv first [past fresh]) (apply (comp not =)))
         "something wrong"
         (->> (mapv #(-> % last type) [past fresh]) (apply (comp not =)))
         "something wrong"
         :else)))

(defn- low-to-low [past fresh]
  (mapv subtract-if-num-kv past fresh))

(defn- diff-kube [past fresh]
  (->> (mapv vector past fresh)
       (mapv
        (fn [[p f]]
          (->> (low-to-low p f)
               (apply merge)))
        )))

(defn compare-kube [past fresh]
  (let [[t-names a-names e-names] (diff-kube-names past fresh)]
    {:terminated
     (filter-by-names (or t-names []) past),
     :existed
     (let [e-fresh (filter-by-names (or e-names []) fresh)]
       [e-fresh
        (diff-kube
         (filter-by-names (or e-names []) past)
         e-fresh)])
     :added
     (filter-by-names (or a-names []) fresh),
    }))

(defn- fetch [kube-keyword]
  ;;TODO: implement
  (list {} {})
  )

(defn analyze [timestamp]
  (apply merge
   (map
    (fn [kube-keyword]
      {kube-keyword
       (->> (fetch kube-keyword)
            (apply compare-kube))})
    [:top-node
     ;; :top-pod-prod :top-pod-dev :hpa-prod :hpa-dev
     ])))
