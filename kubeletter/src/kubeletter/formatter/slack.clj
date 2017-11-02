(ns kubeletter.formatter.slack
  (:require [clojure.data.json :as json]
            [kubeletter.utils.math :as math]
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
        abs-num (-> number (* -1) (max number) (math/roundf 2))
        sign (if (>= number 0) " *↑*" " *↓*")
        format-prefix (if (>= number 0) (str "*`" ) (str "*_"))
        format-suffix (str/reverse format-prefix)]
    (if (zero? number) ""
        (str sign " " format-prefix abs-num unit format-suffix))))


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
  (if (empty? data) nil
      {"pretext" "Removed",
       "text"
       (->> data
            (map #(% "NAME"))
            (join "\n")
            (if (empty? data) "-"))
       "color" "#FFA500",
       "mrkdwn_in" ["text", "pretext", "fields"]}))

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
       add-title-to-added
       (if (empty? data) nil)))

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
     (if-not (number? val-atom) val-atom
             (-> val-atom (/ num) float (math/roundf 2))))
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

(defn- merge-s-fields [left right]
  (let [additional-stuff (-> left (dissoc "title") (dissoc "value"))
        l-title (left "title") r-title (right "title")
        l-val (left "value") r-val (right "value")]
    (-> {"title" (str l-title " / " r-title)
         "value" (str l-val " / " r-val)}
        (merge additional-stuff))))

(defn- node-count-fields [count-changes]
  (let [added-count (count-changes :added)
        removed-count (count-changes :removed)]
    (->> [(s-field "Removed"
                   (if (= 0 removed-count) "-"
                       (str "*↓* *_" removed-count "_*")))
          (s-field "Added"
                   (if (= 0 added-count) "-"
                       (str "*↑* *`" added-count "`*")))]
         (apply merge-s-fields)
         vector)))

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
  {"text" "*[Node Top]* compare to _1 hour ago_",
   "username" "kubeletter",
   "mrkdwn" true,
   "attachments"
   (->> [[(-> data cook-node-summary)]
         (-> (:existed data) cook-node-existed vec)
         [(-> (:terminated data) cook-node-removed)]
         (-> (:added data) cook-node-added vec),
         ]
        (apply concat)
        (remove nil?)
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
