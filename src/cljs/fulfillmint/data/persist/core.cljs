(ns ^{:author "Daniel Leong"
      :doc "core"}
  fulfillmint.data.persist.core)

(defprotocol IDataPersister
  "Anything that can persist the DB data"
  (load-db [this] "Create the initial DB instance, possibly trigger loading.
                   Returns nil if there was no DB")
  (write-db [this conn] "Persist the given db 'connection.' You will need to
                         dereference it to get the current state"))
