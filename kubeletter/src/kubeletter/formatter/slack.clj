(ns kubeletter.formatter.slack
  (:require [clojure.data.json :as json]
            [clojure.string :as str :refer [join trim]]))

(defn- cook-etc [data]
  data)

(defn- top-node-map [rows]
  (->> rows
       (map (fn [row] {(row "NAME") row}))
       (apply merge)))

(defn str-comp-field [comp]
  ;; ex) {"title" "CPU(cores)", "value" "1229m *↑* *`30m`*", "short" true}
  (let [[number unit] comp
        sign-prefix (if (>= number 0) "*↑*" "*↓*")
        format-prefix (if (>= number 0) (str "*`" ) (str "*_"))
        format-suffix (str/reverse format-prefix)]
    (if (zero? number) ""
        (str sign-prefix " " format-prefix (max number (* -1 number)) unit format-suffix))))


(defn- top-node-field
  ([field curr]
   (let [curr-val (->> (curr field) (apply str))]
     {"title" field, "value" curr-val, "short" true}))
  ([field curr comp]
   (let [curr-val (->> (curr field) (apply str))
         comp-val (comp field)
         value (->> comp-val str-comp-field (str curr-val " ") trim)]
     {"title" field, "value" value, "short" true})))

(defn- add-pretext-to-list [data title]
  (let [head (first data), tail (rest data)]
    (-> (merge {"pretext" title} head)
        vector
        (concat tail))))

(defn- add-title-to-added [data]
  (add-pretext-to-list data "Added"))

(defn- add-title-to-existed [data]
  (add-pretext-to-list data "Indivisuals"))

(defn- cook-node-removed [data]
  {"pretext" "Removed",
   "text"
   (->> data
        (map #(% "NAME"))
        (join "\n")
        (if (empty? data) "-"))
   "color" "#FFA500",
   "mrkdwn_in" ["text", "pretext", "fields"]})

(defn- cook-node-added [data]
  (->> data
       (map
        (fn [row]
          (let [row-name (row "NAME")]
            {"title" row-name,
             "color" "#1E90FF",
             "mrkdwn_in" ["text" "pretext" "fields"],
             "fields"
             (->> ["CPU%" "MEMORY%" "CPU(cores)" "MEMORY(bytes)"]
                  (map #(top-node-field % row))
                  vec)})))
       (if (empty? data) [{"title" "-"}])
       add-title-to-added))

(defn- cook-node-existed [data]
  (let [comp-map (top-node-map (last data))]
    (->> (first data)
         (map
          (fn [row]
            (let [row-name (row "NAME")]
              {"title" row-name,
               "color" "gray",
               "mrkdwn_in" ["text" "pretext" "fields"],
               "fields"
               (->> ["CPU%" "MEMORY%" "CPU(cores)" "MEMORY(bytes)"]
                    (map #(top-node-field % row (comp-map row-name)))
                    vec)})))
         add-title-to-existed)))

(defn- node-count-changes [terminated existed added]
  {:current
   (-> (first existed)
       count
       (+ (count added)))
   :added (count added)
   :removed
   (count terminated)
   })

(defn- s-field
  ([title value]
   (s-field title value true))
  ([title value short]
   {"title" title "value" value "short" short}))

(defn- s-field-divider [title]
  (s-field "" title false))

(defn- sum-val [[l-key l-val] [r-key r-val]]
  {l-key
   (map
    (fn [left right]
      (cond
        (= (type left) (type 0))
        (+ left right),
        :else left))
    l-val r-val)})

(defn- divide-by [val num]
  (map
   (fn [val-atom]
     (cond
       (= (type val-atom) (type 0))
       (-> val-atom (/ num) float),
       :else val-atom))
   val))

(defn- sum-row [left right]
  (->> (-> (map #(dissoc % "NAME") [left right])
           (conj sum-val))
       (apply map)
       (apply merge)))

(defn- node-avg-simple [comps]
  (if (empty? comps) {}
      (->> comps
           (reduce sum-row)
           (map (fn [[k v]] {k (divide-by v (count comps))}))
           (apply merge))))

(defn- node-avg-fields [existed]
  (let [node-avg-curr (->> (first existed) node-avg-simple)
        node-avg-comp (->> (last existed) node-avg-simple)]
    (->> node-avg-curr
         (map
          (fn [[key val]]
            (let [comp-val (-> key node-avg-comp str-comp-field)]
              (->> (-> (apply str val) (str " " comp-val) trim)
                   (s-field key))))))))

(defn- node-count-fields [count-changes]
  (let [added-count (count-changes :added)
        removed-count (count-changes :removed)]
    [(s-field "Removed"
              (if (= 0 removed-count) "-"
                  (str "*↓* *_" removed-count "_*")))
     (s-field "Added"
              (if (= 0 added-count) "-"
                  (str "*↑* *`" added-count "`*")))]))

(defn- node-summary [terminated existed added]
  (let [count-changes (node-count-changes terminated existed added)]
    {:node-count (count-changes :current)
     :fields
     (vec
      (-> (node-count-fields count-changes)
          (conj (s-field-divider "Average"))
          (concat
           (let [[cpu-core cpu-percent mem-bytes mem-percent] (node-avg-fields existed)]
             [cpu-percent mem-percent cpu-core mem-bytes])
           )))
     }))

(defn- cook-node-summary [data]
  (let [summary (->> [:terminated :existed :added]
                     (map #(data %))
                     (apply node-summary))
        node-count (summary :node-count)
        fields (summary :fields)]
    {"title" "Node count",
     "color" "#36a64f",
     "pretext" "*Summary*",
     "text" (str "*" node-count "* nodes"),
     "mrkdwn_in" ["text" "pretext" "fields"],
     "fields" fields
     }))

(defn- cook-top-node [data]
  {"text" "*[Node top]* Compare to this _1 hour ago_",
   "username" "kubeletter",
   "mrkdwn" true,
   "attachments"
   (->> [[(-> data cook-node-summary)]
         (-> (:existed data) cook-node-existed vec)
         [(-> (:terminated data) cook-node-removed)]
         (-> (:added data) cook-node-added vec),
         ]
        (apply concat)
        vec),
   })

(defn- cook-by-job [job-id data]
  {job-id
   (case job-id
     :top-node (cook-top-node data)
     (cook-etc data))})

(defn cook [msg]
  (->> msg
       (map (fn [[job-id job-result]]
              (cook-by-job job-id job-result)))
       (apply merge)))
