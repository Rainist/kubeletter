(defproject kubeletter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [im.chit/hara.io.scheduler "2.5.10"]
                 [me.raynes/conch "0.8.0"]
                 [clj-time "0.14.0"]
                 ]
  :main ^:skip-aot kubeletter.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
