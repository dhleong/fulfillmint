(ns ^{:author "Daniel Leong"
      :doc "Interaction with re-frame DB"}
  fulfillmint.data.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [fulfillmint.data.db :as db]))

(defn insert-id [item]
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

(reg-sub
  :part
  :<- [::db]
  (fn [db [_ entity-id]]
    (-> (db/entity-by-id db (int entity-id))
        insert-id
        (as-> e
          (assoc e :service-id (first (:service-ids e)))))))

(defn inflate-variant [parts variant]
  (-> variant
      :variant
      (update :variant/parts
              (fn [part-uses]
                (js/console.log "INFLATE" part-uses " FROM " parts )
                (map
                  (fn [part-use]
                    (assoc part-use
                           :part
                           (get parts (-> part-use
                                          :part-use/part
                                          :db/id))))
                  part-uses)))))

(defn order-from
  [db products variants parts order-id]
  (let [o (db/entity-by-id db (int order-id))]
    (-> o
        insert-id
        (assoc :service-id (first (:service-ids o)))
        (dissoc :order/items)
        (assoc :items
               (map (fn [item]
                      (let [raw-variants (map (fn [{:db/keys [id]}]
                                                (get variants id))
                                              (:order-item/variants item))]
                        (-> item
                            insert-id
                            (dissoc :order-item/variants :order-item/product)
                            (assoc :product (-> raw-variants first :product))
                            (assoc :variants (map (partial inflate-variant parts)
                                                  raw-variants)))))
                    (:order/items o))))))

(reg-sub
  :order
  (fn [[_ order-id]]
    [(subscribe [::db])
     (subscribe [:products-by-order order-id])
     (subscribe [::variants-by-id-for-orders])
     (subscribe [::parts-by-id-for-orders])])
  (fn [[db products variants parts] [_ order-id]]
    (order-from db products variants parts order-id)))

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


; ======= order inflation =================================

(defn products-by-order
  [db order-id]
  (->> (db/products-by-order db (int order-id))
       (map insert-id)))

(reg-sub
  :products-by-order
  :<- [::db]
  (fn [db [_ order-id]]
    (products-by-order db order-id)))


; ======= for reports =====================================

(def parts-for-orders
  (comp
    (partial map (fn [entry]
                   (update entry :part insert-id)))
    db/parts-for-orders))

(reg-all-of-query-sub
  :parts-for-orders
  parts-for-orders)

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


(def variants-for-orders
  (comp
    (partial map (fn [entry]
                   (-> entry
                       (update :variant insert-id)
                       (update :product insert-id))))
    db/variants-for-orders))

(reg-all-of-query-sub
  :variants-for-orders
  variants-for-orders)


(defn ->by-id-for-orders
  ([entities]
   (->by-id-for-orders entities nil))
  ([entities _]
   (->> entities
        (reduce
          (fn [m entity]
            (assoc m (:id entity) entity))
          {}))))

(reg-sub
  ::parts-by-id-for-orders
  :<- [:parts-for-orders]
  (fn [parts-for-orders _]
    (->> parts-for-orders
         (map :part)
         ->by-id-for-orders)))

(reg-sub
  ::variants-by-id-for-orders
  :<- [:variants-for-orders]
  (comp ->by-id-for-orders))
