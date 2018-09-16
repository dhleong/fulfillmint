(ns ^{:author "Daniel Leong"
      :doc "Interaction with re-frame DB"}
  fulfillmint.data.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [fulfillmint.data.db :as db]))

(defn- insert-id [item]
  (-> item
      (dissoc :db/id)
      (assoc :id (:db/id item))))

(defn reg-all-of-query-sub
  [sub-name query-fn]
  (reg-sub
    sub-name
    :<- [::db]
    (fn [db _]
      (->> (query-fn db)
           (map insert-id)))))

(reg-all-of-query-sub :orders db/all-orders)
(reg-all-of-query-sub :parts db/all-parts)
(reg-all-of-query-sub :products db/all-products)

(reg-sub
  :variants-of
  :<- [::db]
  (fn [db [_ product-id]]
    (->> (db/variants-for-product db (or (:id product-id)
                                         product-id))
         (map insert-id))))

(doseq [sub-name [:order :part]]
  (reg-sub
    sub-name
    :<- [::db]
    (fn [db [_ entity-id]]
      (-> (db/entity-by-id db (int entity-id))
          insert-id
          (as-> e
            (assoc e :service-id (first (:service-ids e))))))))

(reg-sub
  :product
  :<- [::db]
  (fn [db [_ product-id]]
    (-> (db/product-by-id db (int product-id))
        insert-id
        (update :variants
                (partial map (fn [v]
                               (-> v
                                   insert-id
                                   (update :variant/parts
                                           (partial map insert-id)))))))))


; ======= order filters ===================================

(reg-sub
  :orders-by-product
  :<- [::db]
  (fn [db [_ product-id]]
    (->> (db/orders-by-product db (int product-id))
         (map insert-id))))


; ======= for reports =====================================

(reg-all-of-query-sub
  :parts-for-orders
  (comp
    (partial map (fn [entry]
                   (update entry :part insert-id)))
    db/parts-for-orders))

(reg-sub
  :part-uses-for-part
  :<- [::db]
  (fn [db [_ part-id]]
    (->> (db/part-uses-for-part db (int part-id))
         (map (fn [entry]
                (-> entry
                    (update :product insert-id)
                    (update :variant insert-id)))))))

(reg-sub
  :parts-for-product
  :<- [::db]
  (fn [db [_ product-id]]
    (as-> (db/parts-for-product db (int product-id))
      m
      (reduce-kv (fn [m k v]
                   (update m k insert-id))
                 m m))))

(reg-all-of-query-sub
  :variants-for-orders
  (comp
    (partial map (fn [entry]
                   (-> entry
                       (update :variant insert-id)
                       (update :product insert-id))))
    db/variants-for-orders))
