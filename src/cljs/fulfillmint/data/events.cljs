(ns ^{:author "Daniel Leong"
      :doc "events"}
  fulfillmint.data.events
  (:require [re-frame.core :refer [dispatch reg-event-db reg-event-fx
                                   path
                                   inject-cofx trim-v]]
            [fulfillmint.data.db :as db]
            [fulfillmint.data.compat :refer [reg-event-conn]]))

(defn- reg-create-event-conn
  [event-name f]
  (reg-event-conn
    event-name
    [trim-v]
    (fn [conn [thing]]
      (f conn thing))))

(reg-create-event-conn
  :create-order
  db/create-order)

(reg-create-event-conn
  :create-part
  db/create-part)

(reg-create-event-conn
  :create-product
  db/create-product)
