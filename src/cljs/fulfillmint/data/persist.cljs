(ns ^{:author "Daniel Leong"
      :doc "Data persistence"}
  fulfillmint.data.persist
  (:require [datascript.core :as d]
            [fulfillmint.data.persist.core :as p]
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

(defn listen!
  "Listen to changes to `conn` and automatically persist it.
   Returns the same `conn`"
  [conn]

  (when-not goog.DEBUG
    ; NOTE: currently disabled in debug mode, since
    ; we're experimenting heavily with the schema
    (d/listen! conn :save (partial p/write-db conn)))

  conn)

(defn unlisten! [conn]
  (d/unlisten! conn :save)
  conn)
