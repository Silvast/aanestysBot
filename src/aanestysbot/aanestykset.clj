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

(def count-url "https://avoindata.eduskunta.fi/api/v1/tables/counts")
(def url "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestys/rows/13265")
(def batch-url "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestys/batch?pkName=AanestysId&pkStartValue=45554")


(defn get-aanestykset []
  (let [aanestykset 
        (client/get url)]
  (log/info aanestykset)))

(def aanestykset (json/read-json (:body (client/get url))))

(println aanestykset)


 (filter
  #(= (:tableName %) "SaliDBAanestys")
  (json/read-json (:body (client/get count-url))))

(defn get-row-count []
       (let [count-data 
             (-> (client/get count-url)
                 (:body)
                 (json/read-json))]
           (:rowCount  
            (first (filter
              #(= (:tableName %) "SaliDBAanestys")
              count-data)))
         ))
   
(get-row-count)

(get-new-votes [last-vote]
               
               )
 
 ;; EKA LAMBDA
 ;; ==========
 ;; katso firebasesta (ehkä dynamosta?) edellinen äänestysnumero
 ;; katso, kuinka monta äänestystä since edellinen == rowCount
 ;; työnnä jonoon kaikki äänestykset 
 ;; TOKA LAMDA
 ;; ==========
 ;; väijy jonoa
 ;; jos uuusi äänestys --> twiittaa
 ;; jos ei, do nothing
 ;; profit
 