(ns kubeletter.formatter.slack
  (:require [clojure.data.json :as json]
            [kubeletter.utils.math :as math]
            [clojure.string :as str :refer [join trim]]))

(defn get-num-or-default-from-env [env-key default]
  (let [env (System/getenv env-key)]
    (-> env
        empty?
        (if default (read-string env)))))

(def ^:private MEMORY_DANGER_LEVEL
  (-> "MEMORY_DANGER_LEVEL_PERCENT"
      (get-num-or-default-from-env 80)))

(def ^:private CPU_DANGER_LEVEL
  (-> "CPU_DANGER_LEVEL_PERCENT"
      (get-num-or-default-from-env 50)))

(def ^:private tag-room "<!here>")

(defn- cook-etc [data]
  data)

(defn- top-node-map [rows]
  (->> rows
       (map (fn [row] {(row "NAME") row}))
       (apply merge)))

(defn- danger-val? [key val]
  (let [[num unit] val
        danger-level (case key "CPU%" CPU_DANGER_LEVEL "MEMORY%" MEMORY_DANGER_LEVEL 999999)]
    (> num danger-level)))

(defn- cook-node-val [key val]
  (let [[num unit] val]
    (if (danger-val? key val)
        (str "`" num "`" unit)
        (str num unit))))

(defn str-comp-field [compared]
  ;; ex) {"title" "CPU(cores)", "value" "1229m *↑* *`30m`*", "short" true}
  (let [[number unit] compared
        abs-num (-> number (* -1) (max number) (math/roundf 2))
        sign (if (>= number 0) " *↑*" " *↓*")
        format-prefix (if (>= number 0) (str "*`" ) (str "*_"))
        format-suffix (str/reverse format-prefix)]
    (if (zero? number) ""
        (str sign " " format-prefix abs-num unit format-suffix))))

(defn- top-node-field
  ([field curr]
   (let [curr-val (cook-node-val field (curr field))]
     {"title" field, "value" curr-val, "short" true}))
  ([field curr compared]
   (let [curr-val (cook-node-val field (curr field))
         comp-val (compared field)
         value (->> comp-val str-comp-field (str curr-val " ") trim)]
     {"title" field, "value" value, "short" true})))

(defn- add-pretext-to-list [data title]
  (let [head (first data), tail (rest data)]
    (-> (merge {"pretext" title} head)
        vector
        (concat tail))))

(defn- add-title-to-added [data]
  (add-pretext-to-list data "Added"))

(defn- add-title-to-existed [title warn? data]
  (->> (if warn? (str title " " tag-room) title)
       (add-pretext-to-list data)))

(defn- compact-node-name [node-name]
  (-> node-name (str/split #"\.") first))

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
       (sort-by #(-> % (get "CPU%") first) >)
       (map
        (fn [row]
          (let [row-name (row "NAME")]
            {"title" (-> row-name compact-node-name),
             "color" "#1E90FF",
             "mrkdwn_in" ["text" "pretext" "fields"],
             "fields"
             (->> ["CPU%" "MEMORY%" "CPU(cores)" "MEMORY(bytes)"]
                  (map #(top-node-field % row))
                  vec)})))
       (if (empty? data) [{"title" "-"}])
       add-title-to-added
       (if (empty? data) nil)))

(defn- dangerous-row? [row]
  (or (danger-val? "CPU%" (row "CPU%"))
      (danger-val? "MEMORY%" (row "MEMORY%"))))

(defn- existed-each-field [row comp-map]
  (let [row-name (row "NAME")
        row-color (-> (or (danger-val? "CPU%" (row "CPU%"))
                          (danger-val? "MEMORY%" (row "MEMORY%")))
                      (if "red" "gray"))]
    {"title" (-> row-name compact-node-name),
     "color" row-color,
     "mrkdwn_in" ["text" "pretext" "fields"],
     "fields"
     (->> ["CPU%" "MEMORY%" "CPU(cores)" "MEMORY(bytes)"]
          (mapv #(top-node-field % row (comp-map row-name))))}))

(defn- limitable-num? [limit-num]
  (= limit-num (Math/abs limit-num)))

(defn- limit-existed [limit-count existed-list]
  (->> (let [[head tail] (split-at limit-count existed-list)]
         (->> tail
              (filter dangerous-row?)
              (concat head)))
       (if-not (limitable-num? limit-count) existed-list)))

(defn- cook-node-existed-limit [[curr compared] limit]
  (let [comp-map (top-node-map compared)
        title (if (limitable-num? limit) (str "*Indivisuals* _top " limit "(+)_") "Indivisuals")
        should-warn? (->> curr (map dangerous-row?) (reduce #(or %1 %2)))]
    (->> curr
         (sort-by #(-> % (get "CPU%") first) >)
         (limit-existed limit)
         (map #(existed-each-field % comp-map))
         (add-title-to-existed title should-warn?))))

(def ^:private fallback-default-limit 5)

(defn- decide-limit-value [val]
  (->> (let [stringified (str val)
             numified (read-string stringified)
             is-num? (number? numified)]
         (if is-num? numified fallback-default-limit))
       (if-not val fallback-default-limit)))

(def ^:private LIMIT_ENV_KEY "INDIVISUAL_COUNT_LIMIT")
(def ^:private INDIVISUAL_LIMIT (decide-limit-value (System/getenv LIMIT_ENV_KEY)))

(defn- cook-node-existed [[curr compared]]
  (cook-node-existed-limit [curr compared] INDIVISUAL_LIMIT))

(defn- s-field
  ([title value]
   (s-field title value true))
  ([title value short]
   {"title" title "value" value "short" short}))

(defn- s-field-divider [title]
  (s-field "" title false))

(defn- cpu-core-convert [cpu-core]
  (let [reg #"([0-9|\.]+)(.+)"
        [_ num-val-str unit] (re-find reg cpu-core)
        num-val (-> num-val-str read-string)]
    (if-not (= unit "m")
      cpu-core
      (-> (math/roundf (/ num-val 1000) 1)
          str))))

(defn- mem-byte-convert [mem-byte]
  (let [reg #"([0-9|\.]+)(.+)"
        [aa num-val-str unit] (re-find reg mem-byte)
        num-val (-> num-val-str read-string)]
    (if-not (= unit "Mi")
      mem-byte
      (-> (math/roundf (/ num-val 1000) 1)
          (str "Gi")))))

(defn- bold-only-first [bits]
  (->> (str "*" (first bits) "*")
       (conj (rest bits))
       (str/join " ")))

(defn- top-node-field-compact [row option]
  (let [fields-map (->> (row "fields") (map #(hash-map (% "title") (% "value"))) (apply merge))

        pure-cores (-> fields-map (get "CPU(cores)") (str/split #" ") first cpu-core-convert)
        pure-bytes (-> fields-map (get "MEMORY(bytes)") (str/split #" ") first mem-byte-convert)

        [cpu-per-field mem-per-field] (->> ["CPU%" "MEMORY%"]
                                           (map #(-> (fields-map %)
                                                     (str/split #" ")
                                                     bold-only-first)))

        no-title? (= option :no-title)
        cpu-title (if no-title? "" "CPU")
        mem-title (if no-title? "" "MEMORY")]

    (-> row
        (dissoc "fields")
        (merge {"fields"
                (-> (->> (row "fields")
                         (filterv (fn [f-row]
                                    (not-any? #(= % (f-row "title")) ["CPU%" "CPU(cores)" "MEMORY%" "MEMORY(bytes)"]))))
                    (concat [(s-field cpu-title (str "_" pure-cores "_->" cpu-per-field))
                             (s-field mem-title (str "_" pure-bytes "_->" mem-per-field))])
                    vec)}))))

(defn- compact-fields
  ([row-has-fields]
   (compact-fields row-has-fields nil))
  ([row-has-fields option]
   (->> row-has-fields (map #(top-node-field-compact % option)))))

(defn- node-count-changes [terminated existed added]
  {:current
   (-> (first existed)
       count
       (+ (count added)))
   :added (count added)
   :removed
   (count terminated)
   })

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
              (->> (-> (cook-node-val key val) (str " " comp-val) trim)
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
        node-count-field (s-field "Node count" (str "*" node-count "* nodes"))
        other-fields (summary :fields)
        fields (concat [node-count-field] other-fields)]
    {"color" "#36a64f",
     "pretext" "*Summary*",
     "mrkdwn_in" ["text" "pretext" "fields"],
     "fields" fields
     }))

(defn- cook-top-node [data]
  {"text" "*[Node Top]* compare to _1 hour ago_",
   "username" "kubeletter",
   "mrkdwn" true,
   "attachments"
   (->> [(-> [(-> data cook-node-summary)] compact-fields vec)
         (-> (:existed data) cook-node-existed (compact-fields :no-title) vec)
         [(-> (:terminated data) cook-node-removed)]
         (-> (:added data) cook-node-added (compact-fields :no-title) vec),
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
