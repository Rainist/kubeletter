(ns kubeletter.utils.math
  (:require [clojure.math.numeric-tower :as math]))

(defn roundf
  ([fv] (-> fv math/round))
  ([fv digit]
   (let [pow-ed (math/expt 10 digit)
         f-rounded (-> fv (* pow-ed) math/round (/ pow-ed) float)]
     (if (= f-rounded (-> f-rounded math/round float))
       (math/round f-rounded)
       f-rounded))))

