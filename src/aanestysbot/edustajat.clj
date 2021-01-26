(ns aanestysbot.edustajat
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [clojure.string :as s]))

(defn get-edustaja-url [id page]
  (str "https://avoindata.eduskunta.fi/api/v1/tables/SaliDBAanestysEdustaja/rows?columnName=AanestysId&columnValue=" id "&page=" page))

(defn create-tweetable-data
  [data]
  (let [keys (map keyword (:columnNames data))
        formatted-data (map #(zipmap keys %) (:rowData data))]
    formatted-data))

(defn trunc [s n]
  (let [minified-string (subs s 0 (min (count s) n))]
    (if (< (count minified-string) (count s))
      (str minified-string "..")
      s)))

(defn form-better-voting-url [url]
  (let [numbers (-> url (s/replace "https://www.eduskunta.fi/aanestystulos/" "") (s/split #"\/"))]
    (str "https://www.eduskunta.fi/FI/Vaski/sivut/aanestys.aspx?aanestysnro=" (first numbers) "&istuntonro=" (second numbers)
         "&vuosi=" (nth numbers 2))))

(defn get-random-mp-vote [id kohta pk]
  (let [url-1 (get-edustaja-url id 0)
        url-2 (get-edustaja-url id 1)
        mp (rand-int 200)
        data-1 (json/read-json (:body (client/get url-1)))
        data-2 (json/read-json (:body (client/get url-2)))
        all-data (concat (create-tweetable-data data-1) (create-tweetable-data data-2))
        final-data (conj (nth all-data mp) {:kohta kohta} {:pk pk})]
    (format "Edustaja %s %s (%s) äänesti %s, äänestyksessä: %s - ks. %s" (:EdustajaEtunimi final-data) (:EdustajaSukunimi final-data) (s/trim (:EdustajaRyhmaLyhenne final-data)) (s/trim (:EdustajaAanestys final-data)) (trunc (:kohta final-data) 110) (form-better-voting-url (:pk final-data)))))

;; (def testaus (get-random-mp-vote 45511 "Hallituksen esitys eduskunnalle laiksi varhaiskasvatuksen asiakasmaksuista annetun lain 5 ja 8 §:n muuttamisesta muuttamisesta muuttamisesta" "https://www.eduskunta.fi/aanestystulos/155/161/2020"))
