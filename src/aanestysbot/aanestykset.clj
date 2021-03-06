(ns aanestysbot.aanestykset
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [cognitect.aws.client.api :as aws]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [cognitect.aws.credentials :as credentials]))

(gen-class
 :name "aanestysHandler"
 :methods [[^:static handler
            [com.amazonaws.services.lambda.runtime.events.ScheduledEvent
             com.amazonaws.services.lambda.runtime.Context] void]])

(def cred-provider (credentials/basic-credentials-provider
                    {:access-key-id     (env :aws-access-key)
                     :secret-access-key (env :aws-secret-key)}))

(def queue-url (env :queue-url))

(def sqs (aws/client {:api :sqs}))

(def ddb (aws/client {:api :dynamodb}))

(defn get-start-value []
  (log/info "Haetaan start-value")
  (let [start-value (get-in
                     (aws/invoke ddb {:op :GetItem
                                      :request {:TableName "uusi"
                                                :Key {"id" {:S "1"}}}})
                     [:Item :startvalue :S])]
    (log/info "start-value alussa " start-value)
    start-value))

(defn get-latest-votings []
  (let [start-value (get-start-value)
        batch-base-url "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestys/batch?pkName=AanestysId&pkStartValue="
        batch-url (str batch-base-url  start-value)]
    (json/read-json (:body (client/get batch-url)))))

(defn create-tweetable-data
  [data]
  (into {} [[:url (str "https://www.eduskunta.fi" (:Url data))]
            [:kohta (:KohtaOtsikko data)]
            [:asettelu (:AanestysOtsikko data)]
            [:poytakirja (str "https://www.eduskunta.fi/FI/vaski/Poytakirja/Sivut" (s/replace (:AanestysPoytakirjaUrl data) "valtiopaivaasiakirjat" ""))]
            [:jaa (s/trim (:AanestysTulosJaa data))]
            [:ei (s/trim (:AanestysTulosEi data))]
            [:tyhjia (s/trim (:AanestysTulosTyhjia data))]
            [:poissa (s/trim (:AanestysTulosPoissa data))]
            [:id (s/trim (:AanestysId data))]]))

(defn get-voting-data
  []
  (let [latest-votings (get-latest-votings)
        keys (map keyword (:columnNames latest-votings))
        all-voting-data (map #(zipmap keys %) (:rowData latest-votings))
        fi-voting-data (filter #(= (:KieliId %) "1") all-voting-data)
        sv-voting-data (filter #(= (:KieliId %) "2") all-voting-data)]
    (map #(create-tweetable-data %) fi-voting-data)))

(defn push-to-queu
  []
  (let [votes (get-voting-data)]
    (if (some? votes)
      (do
        (log/info "sending amount of " (count votes) "to queu")
        (doseq [vote votes]
          (aws/invoke sqs {:op :SendMessage
                           :request
                           {:MessageBody (json/write-str vote)
                            :QueueUrl queue-url
                            :MessageGroupId 1}})))
      (log/info "no new votes"))))

(defn update-start-value []
  (let [latest-votings (get-latest-votings)
        new-start-value (inc (:pkLastValue latest-votings))]
    (log/info "Päivitetään start-value arvoon " new-start-value)
    (aws/invoke ddb {:op :DeleteItem
                     :request {:TableName "uusi"
                               :Key {"id" {:S "1"}}}})
    (aws/invoke ddb {:op :PutItem
                     :request {:TableName "uusi"
                               :Item {"id" {:S "1"} "startvalue" {:S (str new-start-value)}}}})))

(defn -handler [this event context]
  (let [start-value (get-start-value)]
    (log/info "Käsitellään" start-value "jälkeen tulleet äänestykset")
    (push-to-queu)
    (update-start-value)))