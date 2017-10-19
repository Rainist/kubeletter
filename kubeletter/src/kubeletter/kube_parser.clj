(ns kubeletter.kube-parser
  (:use [kubeletter.parser :only (into-map)]
        [clojure.pprint]))

(defn- out [result]
  (:out (:proc result)))

(defn- val-unit [reg value]
  (-> (re-find reg value)
      rest
      vec))

(defn- num-unit [unit value]
  (-> (re-pattern (str "([0-9]+)(" unit ")"))
      (val-unit value)))

(defn- simple-num-unit-fn [unit]
  (fn [value]
    (let [[val unit-val] (num-unit unit value)]
      (list (read-string val) unit-val))))

(defn- read-string-fn []
  (fn [value] (read-string value)))

(defn- targets-fn []
  (fn [value]
    (let [reg #"([0-9]+)(%) / ([0-9]+)%"
          [_ numerator num-unit denominator] (re-find reg value)]
      (list
       (read-string numerator)
       num-unit
       (read-string denominator)))))

(def conversions
  {"CPU(cores)"
   (simple-num-unit-fn "m"),
   "CPU%"
   (simple-num-unit-fn "%"),
   "MEMORY(bytes)"
   (simple-num-unit-fn "Mi"),
   "MEMORY%"
   (simple-num-unit-fn "%"),
   "MINPODS"
   (read-string-fn),
   "MAXPODS"
   (read-string-fn),
   "REPLICAS"
   (read-string-fn),
   "AGE"
   (simple-num-unit-fn "d"),
   "TARGETS"
   (targets-fn),
   })

(defn- converter [key value]
  (if (some #(= key %) (keys conversions))
    {key (-> value ((conversions key)))}
    {key value}))

(defn- convert-row [row]
  (->> (map (fn [[k v]] (converter k v)) row)
       (apply merge)))

(defn- convert [raw-parsed]
  (vec (map convert-row raw-parsed)))

(defn parse-kube [kube-result]
  ;;TODO: must handle about exit code 1
  (let [raw-parsed (into-map (out kube-result))]
    (vec (convert raw-parsed))
    )
  )
