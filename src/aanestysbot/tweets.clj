(ns aanestysbot.tweets
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as rest]))

(gen-class
 :name "aanestysTweetsHandler"
 :methods [[^:static tweetsHandler
            [com.amazonaws.services.lambda.runtime.events.ScheduledEvent
             com.amazonaws.services.lambda.runtime.Context] void]])

  ;; This is the source code for lambda which retrieves new votes and if any, puts them in sqs
;;

(defonce app-consumer-key (env :twitter-consumer-key))
(defonce app-consumer-secret (env :twitter-consumer-secret))
(defonce user-access-token (env :twitter-access-token))
(defonce user-access-token-secret (env :twitter-access-token-secret))

(def creds (oauth/make-oauth-creds
            app-consumer-key
            app-consumer-secret
            user-access-token
            user-access-token-secret))

(def queue-url (env :queue-url))

(def sqs (aws/client {:api :sqs}))

(defn receive-vote []
  (let [result (aws/invoke sqs {:op :ReceiveMessage
                                :request
                                {:QueueUrl queue-url
                                 :MaxNumberOfMessages 1}})
        receipt-handle (:ReceiptHandle (first (:Messages result)))
        vote (json/read-json (:Body (first (:Messages result))))]
    (into [] [receipt-handle vote])))

(defn send-tweet [vote]
  (rest/statuses-update :oauth-creds creds :params
                        {:status
                         (format "%s - jaa: %s - ei: %s - tyhjiä: %s - poissa: %s - äänestys: %s"
                                 (:asettelu vote) (:jaa vote) (:ei vote) (:tyhjia vote)
                                 (:poissa vote) (:url vote))}))

(defn send-tweet-test [vote]
  (log/info "testing tweeting: " vote)
  (println
   (format "%s - jaa: %s - ei: %s - tyhjiä: %s - poissa: %s - äänestys: %s"
           (:asettelu vote) (:jaa vote) (:ei vote) (:tyhjia vote) 
           (:poissa vote) (:url vote))))

;; ;; tweet message
(defn tweet-and-delete-vote []
  (let [[receipt-handle vote] (receive-vote)]
    (send-tweet vote)
    (aws/invoke sqs {:op :DeleteMessage
                     :request
                     {:QueueUrl queue-url
                      :ReceiptHandle receipt-handle}})))

(defn -tweetsHandler [this event context]
  (log/info "Tweeting..")
  (tweet-and-delete-vote))