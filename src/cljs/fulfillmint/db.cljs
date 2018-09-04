(ns fulfillmint.db
  (:require [fulfillmint.data.compat :as data]))

(def default-db
  {:page [:home]
   ::data/state data/initial-db-state})
