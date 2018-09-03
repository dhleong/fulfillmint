(ns ^{:author "Daniel Leong"
      :doc "Abstractions over data store"}
  fulfillmint.data.db
  (:require [datascript.core :as d]
            [fulfillmint.data.persist :as p]))

(def schema {})

;; singleton datascript connection
(defonce conn (let [conn (or (p/load-db)
                             (d/create-conn schema))]
                (d/listen! conn :save (partial p/write-db conn))

                conn))

; hot-reload schema
(when-not (= schema (:schema @conn))
  (set! conn (d/conn-from-datoms (d/datoms @conn :eavt) schema)))

(defn- q [query]
  (d/q query
       @conn))
