(ns kubeletter.harvester.parser
  (:use [clojure.string :only (join split last-index-of trim)]))

(defn- split-indices [line titles]
  (map
   (fn [title]
     [title (last-index-of line title)])
   titles))

(defn- range-end [line titles-and-indices]
  (let [indices (map
                 (fn [[_title index]] index)
                 titles-and-indices)
        altered_indices (vec (map dec (rest indices)))]
    (into
     altered_indices
     [(count line)])))

(defn- split-range [line titles]
  (let [titles-and-indices (split-indices line titles)
        ends (range-end line titles-and-indices)]
    (vec
     (map
      flatten
      (map vector titles-and-indices ends)))))

(defn- parse-line [line titles-and-range]
  (let [titles (map first titles-and-range)
        text-ranges (map rest titles-and-range)
        parsed-list (map
                     (fn [[start end]]
                       (trim (subs line start end)))
                     text-ranges)]
    (zipmap titles parsed-list)))

(defn- package [titles-and-range bodies]
  (vec
   (map
    (fn [line]
      (parse-line line titles-and-range))
    bodies)))

(defn into-map [out]
  (->> (let [header (first out)
             titles (split header #"\s+")
             titles-and-range (split-range header titles)
             bodies (rest out)]
         (package titles-and-range bodies))
       (sort-by #(% "NAME"))
       (into [])))
