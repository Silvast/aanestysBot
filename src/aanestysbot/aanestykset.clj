(ns aanestysbot.aanestykset
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws])
  (:gen-class))

;; TO DO: switch to use configs
(def count-url "https://avoindata.eduskunta.fi/api/v1/tables/counts")
(def url "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestys/rows/13265")
;; TO DO get this from dynamodb 
;; 45553
(def start-value "45440")
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
            [:jaa (:AanestysTulosJaa data)]
            [:ei (:AanestysTulosEi data)]
            [:tyhjia (:AanestysTulosTyhjia data)]
            [:poissa (:AanestysTulosPoissa data)]]))

(defn get-voting-data
  []
  (let [keys (map keyword (:columnNames latest-votings))
        all-voting-data (map #(zipmap keys %) (:rowData latest-votings))
        fi-voting-data (filter #(= (:KieliId %) "1") all-voting-data)
        sv-voting-data (filter #(= (:KieliId %) "2") all-voting-data)]
    (map #(create-tweetable-data %) fi-voting-data)))

(get-voting-data)

(defn push-to-queu
  []
  (let [votes (get-voting-data)]
    (doseq [vote votes]
      (log/info vote))))

(push-to-queu)
(System/getenv "AWS_REGION")

(def sqs (aws/client {:api :sqs}))
(aws/invoke sqs {:op :ListQueues})