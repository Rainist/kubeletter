(defproject kubeletter "0.1.9-SNAPSHOT"
  :description "Report status of k8s cluster"
  :url "https://github.com/rainist/kubeletter"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [im.chit/hara.io.scheduler "2.5.10"]
                 [me.raynes/conch "0.8.0"]
                 [clj-time "0.14.0"]
                 [cheshire "5.8.0"]
                 [clj-http "3.7.0"]
                 [com.taoensso/carmine "2.16.0"]
                 ]
  :main ^:skip-aot kubeletter.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
