(ns kubeletter.stores.mem-store
  )

(def ^:private db {})

(defn set-val [key value]
  (assoc db key value))

(defn get-val [key]
  (key db))

(defn tidy []
  ;;TODO: tidy old data
  )
