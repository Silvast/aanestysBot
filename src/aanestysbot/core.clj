(ns aanestysbot.core
  (:require [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  []
  (log/info "Tää on äänestysbottinen, hihhei!"))


(def sqs (aws/client {:api :sqs}))
(aws/invoke sqs {:op :ListQueues})