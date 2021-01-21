(defproject aanestysbot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.10.3"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/data.json "1.0.0"]
                 [com.cognitect.aws/api "0.8.484"]
                 [com.cognitect.aws/s3 "810.2.801.0"]
                 [com.cognitect.aws/sqs "809.2.784.0"]
                 [com.cognitect.aws/dynamodb "809.2.784.0"]
                 [com.cognitect.aws/endpoints "1.1.11.914"]
                 [environ "1.2.0"]
                 [com.amazonaws/aws-lambda-java-core "1.2.0"]
                 [com.amazonaws/aws-lambda-java-events "2.2.7"]
                 [twitter-api "1.8.0" :exclusions [org.clojure/tools.logging]]]
  :plugins [[lein-environ "1.2.0"]]
  :source-paths ["src"]
  :java-source-paths ["src/java"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})