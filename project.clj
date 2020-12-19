(defproject aanestysbot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.10.3"]
                 [com.cognitect.aws/api "0.8.484"]
                 [com.cognitect.aws/s3 "810.2.801.0"]
                 [com.cognitect.aws/sqs "809.2.784.0"]
                 [com.cognitect.aws/endpoints "1.1.11.914"]]
  :main ^:skip-aot aanestysbot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
