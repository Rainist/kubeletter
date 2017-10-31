(ns kubeletter.utils.math
  (:require [clojure.math.numeric-tower :as math]))

(defn roundf
  ([fv]
   (-> fv math/round))
  ([fv digit]
   (let [aa (math/expt 10 digit)]
     (-> fv (* aa) math/round (/ aa) float))))

