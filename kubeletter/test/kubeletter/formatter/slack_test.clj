(ns kubeletter.formatter.slack-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.data :as data :refer [diff]]
            [kubeletter.formatter.slack :refer :all])
  (:use [clojure.pprint]))

(def
  ^:private
  top-node-samples
  {:terminated
   '[{"NAME" "ip-172-20-10-13.ap-northeast-1.compute.internal",
      "CPU(cores)" (154 "m"),
      "CPU%" (7 "%"),
      "MEMORY(bytes)" (3827 "Mi"),
      "MEMORY%" (48 "%")}]
   :added
   '[{"NAME" "ip-172-20-12-18.ap-northeast-1.compute.internal",
      "CPU(cores)" (365 "m"),
      "CPU%" (9 "%"),
      "MEMORY(bytes)" (7480 "Mi"),
      "MEMORY%" (46 "%")}],
   :existed-curr
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (154 "m"),
      "CPU%" (7 "%"),
      "MEMORY(bytes)" (3827 "Mi"),
      "MEMORY%" (48 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (365 "m"),
      "CPU%" (9 "%"),
      "MEMORY(bytes)" (7480 "Mi"),
      "MEMORY%" (46 "%")}],
   :existed-comp
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (-10 "m"),
      "CPU%" (3 "%"),
      "MEMORY(bytes)" (-600 "Mi"),
      "MEMORY%" (30 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (200 "m"),
      "CPU%" (-7 "%"),
      "MEMORY(bytes)" (500 "Mi"),
      "MEMORY%" (10 "%")}],
   })

(def
  ^:private
  top-node-results
  {:top-node-sample
   {:terminated (top-node-samples :terminated),
    :added (top-node-samples :added),
    :existed (map top-node-samples [:existed-curr :existed-comp])},
   })

(def ^:private top-node-expected
  (json/write-str
   {"text" "*[Node top]* Compare to this _1 hour ago_",
    "username" "kubeletter",
    "mrkdwn" true,
    "attachments"
    [;;summary
     {"title" "Node count",
      "color" "#36a64f",
      "pretext" "*Summary*",
      "text" "*3* nodes",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "removed", "value" "*↓* *_1_*", "short" true}
       {"title" "added", "value" "*↑* *`1`*", "short" true}
       {"title" "", "value" "Average", "short" false}
       {"title" "CPU(cores)",
        "value" "259.5m *↑* *`95.0m`*",
        "short" true}
       {"title" "MEMORY(bytes)",
        "value" "5653.5Mi *↓* *_50.0Mi_*",
        "short" true}
       {"title" "CPU%", "value" "8.0% *↓* *_2.0%_*", "short" true}
       {"title" "MEMORY%", "value" "47.0% *↑* *`20.0%`*", "short" true}]}
     ;;Existed
     {"pretext" "Indivisuals",
      "title" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU(cores)", "value" "154m *↓* *_10m_*", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "3827Mi *↓* *_600Mi_*",
        "short" true}
       {"title" "CPU%", "value" "7% *↑* *`3%`*", "short" true}
       {"title" "MEMORY%", "value" "48% *↑* *`30%`*", "short" true}]}
     {"title" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU(cores)", "value" "365m *↑* *`200m`*", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "7480Mi *↑* *`500Mi`*",
        "short" true}
       {"title" "CPU%", "value" "9% *↓* *_7%_*", "short" true}
       {"title" "MEMORY%", "value" "46% *↑* *`10%`*", "short" true}]}
     ;;removed
     {"pretext" "Removed",
      "text" "ip-172-20-10-13.ap-northeast-1.compute.internal",
      "color" "#FFA500",
      "mrkdwn_in" ["text" "pretext" "fields"]}
     ;;added
     {"pretext" "Added",
      "title" "ip-172-20-12-18.ap-northeast-1.compute.internal",
      "color" "#1E90FF",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU(cores)", "value" "365m", "short" true}
       {"title" "MEMORY(bytes)", "value" "7480Mi", "short" true}
       {"title" "CPU%", "value" "9%", "short" true}
       {"title" "MEMORY%", "value" "46%", "short" true}]}
     ],;; end of attachment
    }))

(->> (-> {:top-node (top-node-results :top-node-sample)}
         cook
         (:top-node))
     (diff (->> top-node-expected json/read-str))
     ((fn [[left right _]] (= left right)))
     is
     (testing "format for slack test")
     (deftest format-for-slack-test))

