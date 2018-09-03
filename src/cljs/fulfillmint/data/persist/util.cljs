(ns ^{:author "Daniel Leong"
      :doc "util"}
  fulfillmint.data.persist.util
  (:require [datascript.core :as d]
            [datascript.transit :as dt]))

(defn inflate-transit
  "Inflate a serialized DB from a transit string"
  [transit-str]
  (d/conn-from-db
    (dt/read-transit-str transit-str)))

(defn serialize-transit
  "Serialize the DB 'connection' as a transit string"
  [conn]
  (dt/write-transit-str conn))
