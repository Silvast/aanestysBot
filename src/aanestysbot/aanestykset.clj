(ns aanestysbot.aanestykset
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws]
            [clojure.string :as s]
            [environ.core :refer [env]])
  (:gen-class))

;; TO DO: switch to use configs

(def queue-url (env :queue-url))

(def sqs (aws/client {:api :sqs}))

(def ddb (aws/client {:api :dynamodb}))

(def start-value (get-in 
                  (aws/invoke ddb {:op :GetItem
                  :request {:TableName "uusi"
                            :Key {"id" {:S "1"}}}}) 
                  [:Item :startvalue :S]))

(def batch-base-url "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestys/batch?pkName=AanestysId&pkStartValue=")

(def batch-url
  (str batch-base-url  start-value))

(def latest-votings (json/read-json (:body (client/get batch-url))))

(defn create-tweetable-data
  [data]
  (into {} [[:url (str "https://www.eduskunta.fi" (:Url data))]
            [:kohta (:KohtaOtsikko data)]
            [:asettelu (:AanestysOtsikko data)]
            [:poytakirja (str "https://www.eduskunta.fi" (:AanestysPoytakirjaUrl data))]
            [:jaa (s/trim (:AanestysTulosJaa data))]
            [:ei (s/trim (:AanestysTulosEi data))]
            [:tyhjia (s/trim (:AanestysTulosTyhjia data))]
            [:poissa (s/trim (:AanestysTulosPoissa data))]]))

(defn get-voting-data
  []
  (let [keys (map keyword (:columnNames latest-votings))
        all-voting-data (map #(zipmap keys %) (:rowData latest-votings))
        fi-voting-data (filter #(= (:KieliId %) "1") all-voting-data)
        sv-voting-data (filter #(= (:KieliId %) "2") all-voting-data)]
    (map #(create-tweetable-data %) fi-voting-data)))

(defn push-to-queu
  []
  (let [votes (get-voting-data)]
    (doseq [vote votes]
      (aws/invoke sqs {:op :SendMessage
                       :request
                       {:MessageBody (json/write-str vote)
                        :QueueUrl queue-url}}))))


(defn update-start-value []
 (aws/invoke ddb {:op :DeleteItem
                 :request {:TableName "uusi"
                           :Key {"id" {:S "1"}}}})
(aws/invoke ddb {:op :PutItem
                 :request {:TableName "uusi"
                           :Item {"id" {:S "1"} "startvalue" {:S (str (inc (:pkLastValue latest-votings)))}}}}))