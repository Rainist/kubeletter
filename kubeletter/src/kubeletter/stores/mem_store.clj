(ns kubeletter.stores.mem-store
  )

(def ^:private db {})

(defn debug []
  (println db))

(defn set-val [key value]
  (->> (assoc db key value)
      (def db)))

(defn get-val [key]
  (db key))

(defn tidy []
  ;;TODO: tidy old data
  )

