(ns aanestysbot.tweets
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as rest]
            [cognitect.aws.credentials :as credentials]))

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

(def cred-provider (credentials/basic-credentials-provider
                    {:access-key-id     (env :aws-access-key)
                     :secret-access-key (env :aws-secret-key)}))

(def queue-url (env :queue-url))

(def sqs (aws/client {:api :sqs :credentials-provider cred-provider}))

(defn receive-vote []
  (let [sqs (aws/client {:api :sqs})
        result (aws/invoke sqs {:op :ReceiveMessage
                                :request
                                {:QueueUrl queue-url
                                 :MaxNumberOfMessages 1}})]
    (log/info result)
    (if (some? (first (:Messages result)))
      (into [] [(:ReceiptHandle (first (:Messages result)))
                (json/read-json (:Body (first (:Messages result))))])
      (log/info "no new votes"))))

(defn send-tweet [vote]
  (let [message  (format 
                  "Äänestys: %s - jaa: %s - ei: %s - tyhjiä: %s - poissa: %s - äänestys: %s" 
                  (:asettelu vote) (:jaa vote) (:ei vote) (:tyhjia vote)
                  (:poissa vote) (:poytakirja vote))]
    (log/info "trying to tweet " message)
    (rest/statuses-update :oauth-creds creds :params
                          {:status message})))

(defn tweet-and-delete-vote []
  (let [[receipt-handle vote] (receive-vote)
         sqs (aws/client {:api :sqs})]
    (if (some? vote)
      (do
        (send-tweet vote)
        (aws/invoke sqs {:op :DeleteMessage
                         :request
                         {:QueueUrl queue-url
                          :ReceiptHandle receipt-handle}}))
      (log/info "vote was nil"))))

(defn -tweetsHandler [this event context]
  (log/info "Tweeting..")
  (tweet-and-delete-vote))
