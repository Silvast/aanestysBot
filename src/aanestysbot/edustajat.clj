(ns aanestysbot.edustajat)



;; hae äänestysid:llä data
;; 
;; laske datasta row count
;; 
;; ota random numero 1-coucount
;; 
;; ota yksi data objekti numerolle
;; 
;; tweettaa "Edustaja xx äänesti yy äänestyksessä (:Äänestyskohta)"
;; 

(defn get-edustaja-url [id page]
  (str "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestysEdustaja/rows?columnName=AanestysId&columnValue=" id "&page=" page))

;; (defn get-random-mp-vote [vote]
;;   (let [id (:id vote)]
;;     (client/get )
;;     )
;;   )