(ns ^{:author "Daniel Leong"
      :doc "Data persistence"}
  fulfillmint.data.persist
  (:require [fulfillmint.data.persist.core :as p]
            [fulfillmint.data.persist.storage :as local]))

; TODO backup to google
(def persister (local/create))

(defn load-db []
  (p/load-db persister))

(defn write-db [conn report]
  ; NOTE "report" has a list of datoms added and temporary
  ; ids resolution table, but we usually just persist the
  ; whole thing...
  (p/write-db persister conn))
