(defproject aanestysbot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-http "3.10.3"]
                 [org.clojure/data.json "1.0.0"]
                 [metosin/jsonista "0.2.6"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [metosin/reitit "0.5.10"]
                 [org.apache.logging.log4j/log4j-api "2.11.1"]
                 [org.apache.logging.log4j/log4j-core "2.11.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.1"]
                 [org.clojure/tools.logging "1.1.0"]
                 [com.amazonaws/aws-xray-recorder-sdk-core "2.4.0"]
                 [com.amazonaws/aws-xray-recorder-sdk-aws-sdk-v2 "2.4.0"]
                 [com.amazonaws/aws-lambda-java-core "1.2.0"]
                 [com.amazonaws/aws-lambda-java-events "2.2.7"]
                 [software.amazon.awssdk/dynamodb "2.10.56"]
                 [software.amazon.awssdk/ssm "2.10.56"]
                 [software.amazon.awssdk/sqs "2.10.60"]]
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.2"]
            [jonase/eastwood "0.3.1"]]
  :main ^:skip-aot aanestysbot.core
  :target-path "target/%s"
  :resource-paths ["configuration"]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[ring/ring-mock "0.3.2"]]}}
    :aliases {"checkall" ["do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]})
