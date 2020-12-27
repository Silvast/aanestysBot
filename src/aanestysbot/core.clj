(ns aanestysbot.core
  (:require [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws]
            [aanestysbot.aanestykset :as aanestykset])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  []
  (log/info "Tää on äänestysbottinen, hihhei!")
  (aanestykset/push-to-queu)
  (aanestykset/update-start-value))