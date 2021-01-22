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
  (let [result (aws/invoke sqs {:op :ReceiveMessage
                                :request
                                {:QueueUrl queue-url
                                 :MaxNumberOfMessages 1}})]
    (if (some? (first (:Messages result)))
    (into [] [(:ReceiptHandle (first (:Messages result))) 
              (json/read-json (:Body (first (:Messages result))))])
     (log/info "no new votes"))))


(defn send-tweet [vote]
  (rest/statuses-update :oauth-creds creds :params
                        {:status
                         (format "%s - jaa: %s - ei: %s - tyhjiä: %s - poissa: %s - äänestys: %s"
                                 (:asettelu vote) (:jaa vote) (:ei vote) (:tyhjia vote)
                                 (:poissa vote) (:url vote))}))

;; (defn send-tweet-test [vote]
;;   (log/info "testing tweeting: " vote)
;;   (log/info
;;    (format "%s - jaa: %s - ei: %s - tyhjiä: %s - poissa: %s - äänestys: %s"
;;            (:asettelu vote) (:jaa vote) (:ei vote) (:tyhjia vote) 
;;            (:poissa vote) (:url vote))))

;; ;; tweet message
(defn tweet-and-delete-vote []
  (let [[receipt-handle vote] (receive-vote)]
    (send-tweet vote)
    (aws/invoke sqs {:op :DeleteMessage
                     :request
                     {:QueueUrl queue-url
                      :ReceiptHandle receipt-handle}})))

;; (defn test-function []
;;   (let [sqsnew (aws/client {:api :sqs})
;;         result (aws/invoke sqsnew {:op :ReceiveMessage
;;                                 :request
;;                                 {:QueueUrl queue-url
;;                                  :MaxNumberOfMessages 1}})]
;;     (log/info result)
;;     (log/info "testiresult " result)
;;     result))

(defn -tweetsHandler [this event context]
  (log/info "Tweeting..")
  (tweet-and-delete-vote))