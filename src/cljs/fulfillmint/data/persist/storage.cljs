(ns ^{:author "Daniel Leong"
      :doc "Local storage persistence"}
  fulfillmint.data.persist.storage
  (:require-macros [fulfillmint.util.log :refer [log]])
  (:require [alandipert.storage-atom :refer [local-storage]]
            [fulfillmint.data.persist.core :refer [IDataPersister]]
            [fulfillmint.data.persist.util :refer [inflate-transit
                                                   serialize-transit]]))

(defonce db-storage (local-storage (atom nil) :fulfillment-db))

(deftype LocalPersister []
  IDataPersister
  (load-db [this]
    (when-let [transit @db-storage]
      (inflate-transit transit)))
  (write-db [this conn]
    (log "Writing DB")
    (reset! db-storage
            (serialize-transit @conn))))

(defn create []
  (->LocalPersister))
