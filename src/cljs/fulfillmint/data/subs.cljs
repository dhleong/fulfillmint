(ns ^{:author "Daniel Leong"
      :doc "Interaction with re-frame DB"}
  fulfillmint.data.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [fulfillmint.data.db :as db]))

(defn- insert-id [item]
  (-> item
      (dissoc :db/id)
      (assoc :id (:db/id item))))

(defn reg-all-of-kind-sub
  [sub-name query-fn]
  (reg-sub
    sub-name
    :<- [::db]
    (fn [db _]
      (->> (query-fn db)
           (map insert-id)))))

(reg-all-of-kind-sub :orders db/all-orders)
(reg-all-of-kind-sub :parts db/all-parts)
(reg-all-of-kind-sub :products db/all-products)
