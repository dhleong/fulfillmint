(ns ^{:author "Daniel Leong"
      :doc "Data storage / query"}
  fulfillmint.data
  (:require [fulfillmint.data.db]))

;; NOTE:
;; Fulfillment data is stored in a DB format that is opaque to other
;; UI code. Currently we use datascript to handle queries and storage,
;; but that could change. The data ns is responsible for creating
;; re-frame subscriptions and events that act as abstractions on top
;; of the underlying storage format
