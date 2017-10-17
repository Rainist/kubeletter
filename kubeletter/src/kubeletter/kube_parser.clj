(ns kubeletter.kube-parser
  (:use [kubeletter.parser :only (into-map)]
        [clojure.pprint]))

(defn- out [result]
  (:out (:proc result)))

(defn- val-unit [reg value]
  (vec (rest (re-find reg value))))

(defn- num-unit [unit value]
  (let [reg-text (str "([0-9]+)(" unit ")")
        reg (re-pattern reg-text)]
    (val-unit reg value)))

(defn- simple-num-unit-fn [unit]
  (fn [value]
    (let [[val unit-val] (num-unit unit value)]
      (list
       (read-string val)
       unit-val))))

(defn- trans-num []
  (fn [value]
    (read-string value)))

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
   (trans-num),
   "MAXPODS"
   (trans-num),
   "REPLICAS"
   (trans-num),
   "AGE"
   (simple-num-unit-fn "d"),
   "TARGETS"
   (fn [value]
     (let [reg #"([0-9]+)(%) / ([0-9]+)%"
           [_ numerator num-unit denominator] (re-find reg value)]
       (list
        (read-string numerator)
        num-unit
        (read-string denominator)))),
   })

(defn- converter [key value]
  (if (some #(= key %) (keys conversions))
    (let [func (conversions key)]
      {key (func value)})
    {key value}))

(defn- convert-row [row]
  (apply
   merge
   (map
    (fn [[k v]]
      (converter k v))
    row)))

(defn- convert [raw-parsed]
  (vec (map convert-row raw-parsed)))

(defn parse-kube [kube-result]
  ;;TODO: must handle about exit code 1
  (let [raw-parsed (into-map (out kube-result))]
    (vec (convert raw-parsed))
    )
  )
