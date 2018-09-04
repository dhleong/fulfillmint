(ns ^{:author "Daniel Leong"
      :doc "events"}
  fulfillmint.data.events
  (:require [re-frame.core :refer [dispatch reg-event-db reg-event-fx
                                   path
                                   inject-cofx trim-v]]
            [fulfillmint.data.db :as db]
            [fulfillmint.data.compat :refer [reg-event-conn]]))


(reg-event-conn
  :create-part
  [trim-v]
  (fn [conn [part]]
    (db/create-part conn part)))

(reg-event-conn
  :create-product
  [trim-v]
  (fn [conn [product]]
    (db/create-product conn product)))
