(ns aanestysbot.aanestykset
  (:require [reitit.ring :as ring]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
    ;       [reitit.ring.middleware.dev :as dev]
    ;       [reitit.ring.spec :as spec]
    ;       [spec-tools.spell :as spell]
            [ring.adapter.jetty :as jetty]
            [muuntaja.core :as m]
            [clojure.java.io :as io]
            [malli.util :as mu]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clj-http.client :as client])
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