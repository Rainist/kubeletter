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
      "CPU(cores)" (154 "m"), "CPU%" (7 "%"), "MEMORY(bytes)" (3827 "Mi"), "MEMORY%" (48 "%")}]
   :added
   '[{"NAME" "ip-172-20-12-18.ap-northeast-1.compute.internal",
      "CPU(cores)" (365 "m"), "CPU%" (9 "%"), "MEMORY(bytes)" (7480 "Mi"), "MEMORY%" (46 "%")}],
   :existed-curr
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (154 "m"), "CPU%" (7 "%"), "MEMORY(bytes)" (3827 "Mi"), "MEMORY%" (48 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (365 "m"), "CPU%" (9 "%"), "MEMORY(bytes)" (7480 "Mi"), "MEMORY%" (46 "%")}],
   :existed-comp
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (-10 "m"), "CPU%" (3 "%"), "MEMORY(bytes)" (-600 "Mi"), "MEMORY%" (30 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (200 "m"), "CPU%" (-7 "%"), "MEMORY(bytes)" (500 "Mi"), "MEMORY%" (10 "%")}],
   :existed-comp-zero
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (0 "m"), "CPU%" (0 "%"), "MEMORY(bytes)" (0 "Mi"), "MEMORY%" (0 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (0 "m"), "CPU%" (0 "%"), "MEMORY(bytes)" (0 "Mi"), "MEMORY%" (0 "%")}],
   :avg-curr
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (154.098954 "m"), "CPU%" (7.90938039 "%"), "MEMORY(bytes)" (3827.039823 "Mi"), "MEMORY%" (48 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (365.102938 "m"), "CPU%" (9.398043948 "%"), "MEMORY(bytes)" (7480.09380398 "Mi"), "MEMORY%" (46 "%")}
     ],
   :avg-comp
   '[{"NAME" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "CPU(cores)" (-10.93080 "m"), "CPU%" (3.09384 "%"), "MEMORY(bytes)" (-600.0938390 "Mi"), "MEMORY%" (30.02394802938 "%")}
     {"NAME" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "CPU(cores)" (200.89307448 "m"), "CPU%" (-7.0938403 "%"), "MEMORY(bytes)" (500.2903848702 "Mi"), "MEMORY%" (10.0293849 "%")}],
   })

(def ^:private top-node-results
  {:mixed
   {:terminated (top-node-samples :terminated),
    :added (top-node-samples :added),
    :existed (map top-node-samples [:existed-curr :existed-comp])},
   :no-added-and-removed
   {:terminated [],
    :added [],
    :existed (map top-node-samples [:existed-curr :existed-comp])},
   :same
   {:terminated [],
    :added [],
    :existed (map top-node-samples [:existed-curr :existed-comp-zero])},
   })

(def ^:private top-node-expected
  {:mixed
   {"text" "*[Node Top]* compare to _1 hour ago_",
    "username" "kubeletter",
    "mrkdwn" true,
    "attachments"
    [;;summary
     {"color" "#36a64f",
      "pretext" "*Summary*",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "Node count", "value" "*3* nodes", "short" true}
       {"title" "Removed / Added", "value" "*↓* *_1_* / *↑* *`1`*", "short" true}
       {"title" "", "value" "Average", "short" false}
       {"title" "CPU%", "value" "8%  *↓* *_2%_*", "short" true}
       {"title" "MEMORY%", "value" "47%  *↑* *`20%`*", "short" true}
       {"title" "CPU(cores)",
        "value" "259.5m  *↑* *`95m`*",
        "short" true}
       {"title" "MEMORY(bytes)",
        "value" "5653.5Mi  *↓* *_50Mi_*",
        "short" true}]}
     ;;Existed
     {"pretext" "Indivisuals",
      "title" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU%", "value" "9%  *↓* *_7%_*", "short" true}
       {"title" "MEMORY%", "value" "46%  *↑* *`10%`*", "short" true}
       {"title" "CPU(cores)", "value" "365m  *↑* *`200m`*", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "7480Mi  *↑* *`500Mi`*",
        "short" true}]}
     {"title" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU%", "value" "7%  *↑* *`3%`*", "short" true}
       {"title" "MEMORY%", "value" "48%  *↑* *`30%`*", "short" true}
       {"title" "CPU(cores)", "value" "154m  *↓* *_10m_*", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "3827Mi  *↓* *_600Mi_*",
        "short" true}]}
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
      [{"title" "CPU%", "value" "9%", "short" true}
       {"title" "MEMORY%", "value" "46%", "short" true}
       {"title" "CPU(cores)", "value" "365m", "short" true}
       {"title" "MEMORY(bytes)", "value" "7480Mi", "short" true}]}
     ],;; end of attachment
    },
   :no-added-and-removed
   {"text" "*[Node Top]* compare to _1 hour ago_",
    "username" "kubeletter",
    "mrkdwn" true,
    "attachments"
    [;;summary
     {"color" "#36a64f",
      "pretext" "*Summary*",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "Node count", "value" "*2* nodes", "short" true}
       {"title" "Removed / Added", "value" "- / -", "short" true}
       {"title" "", "value" "Average", "short" false}
       {"title" "CPU%", "value" "8%  *↓* *_2%_*", "short" true}
       {"title" "MEMORY%", "value" "47%  *↑* *`20%`*", "short" true}
       {"title" "CPU(cores)",
        "value" "259.5m  *↑* *`95m`*",
        "short" true}
       {"title" "MEMORY(bytes)",
        "value" "5653.5Mi  *↓* *_50Mi_*",
        "short" true}]}
     ;;Existed
     {"pretext" "Indivisuals",
      "title" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU%", "value" "9%  *↓* *_7%_*", "short" true}
       {"title" "MEMORY%", "value" "46%  *↑* *`10%`*", "short" true}
       {"title" "CPU(cores)", "value" "365m  *↑* *`200m`*", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "7480Mi  *↑* *`500Mi`*",
        "short" true}]}
     {"title" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU%", "value" "7%  *↑* *`3%`*", "short" true}
       {"title" "MEMORY%", "value" "48%  *↑* *`30%`*", "short" true}
       {"title" "CPU(cores)", "value" "154m  *↓* *_10m_*", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "3827Mi  *↓* *_600Mi_*",
        "short" true}]}
     ;;removed and addded are omitted
     ],;; end of attachment
    },
   :same
   {"text" "*[Node Top]* compare to _1 hour ago_",
    "username" "kubeletter",
    "mrkdwn" true,
    "attachments"
    [;;summary
     {"color" "#36a64f",
      "pretext" "*Summary*",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "Node count", "value" "*2* nodes", "short" true}
       {"title" "Removed / Added", "value" "- / -", "short" true}
       {"title" "", "value" "Average", "short" false}
       {"title" "CPU%", "value" "8%", "short" true}
       {"title" "MEMORY%", "value" "47%", "short" true}
       {"title" "CPU(cores)",
        "value" "259.5m",
        "short" true}
       {"title" "MEMORY(bytes)",
        "value" "5653.5Mi",
        "short" true}]}
     ;;Existed
     {"pretext" "Indivisuals",
      "title" "ip-172-20-39-187.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU%", "value" "9%", "short" true}
       {"title" "MEMORY%", "value" "46%", "short" true}
       {"title" "CPU(cores)", "value" "365m", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "7480Mi",
        "short" true}]}
     {"title" "ip-172-20-62-138.ap-northeast-1.compute.internal",
      "color" "gray",
      "mrkdwn_in" ["text" "pretext" "fields"],
      "fields"
      [{"title" "CPU%", "value" "7%", "short" true}
       {"title" "MEMORY%", "value" "48%", "short" true}
       {"title" "CPU(cores)", "value" "154m", "short" true}
       {"title" "MEMORY(bytes)",
        "value" "3827Mi",
        "short" true}]}
     ;;removed and addded are omitted
     ],;; end of attachment
    },
   })

(deftest format-for-slack-test
  (->> (-> {:top-node (top-node-results :mixed)}
           cook
           (:top-node))
       (diff (->> top-node-expected :mixed))
       ((fn [[left right _]] (= left right)))
       is
       (testing "format for mixed "))

  (->> (-> {:top-node (top-node-results :no-added-and-removed)}
           cook
           (:top-node))
       (diff (->> top-node-expected :no-added-and-removed))
       ((fn [[left right _]] (= left right)))
       is
       (testing "format for no added and removed"))

  (->> (-> {:top-node (top-node-results :same)}
           cook
           (:top-node))
       (diff (->> top-node-expected :same))
       ((fn [[left right _]] (= left right)))
       is
       (testing "format for comparing with sameself"))
  )

(def ^:private node-avg-fields #'kubeletter.formatter.slack/node-avg-fields)

(deftest node-avg-fields-test
  (->> (list (top-node-samples :avg-curr)
             (top-node-samples :avg-comp))
       node-avg-fields
       (diff '({"title" "CPU(cores)",
                "value" "77.05m  *↓* *_5.47m_*",
                "short" true}
               {"title" "CPU%", "value" "3.95%  *↑* *`1.55%`*", "short" true}
               {"title" "MEMORY(bytes)",
                "value" "1913.52Mi  *↓* *_300.05Mi_*",
                "short" true}
               {"title" "MEMORY%", "value" "47%  *↑* *`15.01%`*", "short" true}))
       ((fn [[left right _]] (= left right)))
       is
       (testing "node avg fields")))


(def ^:private cook-node-val #'kubeletter.formatter.slack/cook-node-val)

(deftest cook-node-val-test
  (->> (cook-node-val "CPU(cores)" '(49 "m"))
       (= "49m")
       is
       (testing "cpu(cores) less than 50"))

  (->> (cook-node-val "Memory(bytes)" '(49 "Mi"))
       (= "49Mi")
       is
       (testing "memory(byetes) less than 50"))

  (->> (cook-node-val "MEMORY%" '(79 "%"))
       (= "79%")
       is
       (testing "memory% less than 80"))

  (->> (cook-node-val "MEMORY%" '(81 "%"))
       (= "`81`%")
       is
       (testing "memory% more than 80"))

  (->> (cook-node-val "CPU%" '(49 "%"))
       (= "49%")
       is
       (testing "cpu% less than 50"))

  (->> (cook-node-val "CPU%" '(51 "%"))
       (= "`51`%")
       is
       (testing "cpu% more more 50")))

(def ^:private dangerous-row? #'kubeletter.formatter.slack/dangerous-row?)
(def ^:private add-title-to-existed? #'kubeletter.formatter.slack/add-title-to-existed)

(deftest attach-tagging-test
  (->> {"CPU%" '(51 "%") "MEMORY%" '(10 "%")}
       dangerous-row?
       (= true)
       is
       (testing "dangerous row because of cpu"))

  (->> {"CPU%" '(10 "%") "MEMORY%" '(81 "%")}
       dangerous-row?
       (= true)
       is
       (testing "dangerous row because of memory"))

  (->> {"CPU%" '(10 "%") "MEMORY%" '(10 "%")}
       dangerous-row?
       (= false)
       is
       (testing "not dangerous row"))

  (->> (add-title-to-existed? true [{}])
       (= [{"pretext" "Indivisuals <!here>"}])
       is
       (testing "tagged"))

  (->> (add-title-to-existed? false [{}])
       (= [{"pretext" "Indivisuals"}])
       is
       (testing "not tagged"))
       )
