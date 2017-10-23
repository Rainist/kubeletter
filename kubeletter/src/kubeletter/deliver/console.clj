(ns kubeletter.deliver.console
  )

(defn hand-over [msg]
  (println "Delivering to console")
  (println msg)
  true
  )
