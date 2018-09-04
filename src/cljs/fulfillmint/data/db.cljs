(ns ^{:author "Daniel Leong"
      :doc "Abstractions over data store"}
  fulfillmint.data.db
  (:require [datascript.core :as d]
            [fulfillmint.data.persist :as p]
            [fulfillmint.data.schema :refer [schema]]))

;; singleton datascript connection
(defonce conn (p/listen!
                (or (p/load-db)
                    (d/create-conn schema))))

; hot-reload schema
(when-not (= schema (:schema @conn))
  (p/unlisten! conn)
  (set! conn (p/listen!
               (d/conn-from-datoms (d/datoms @conn :eavt) schema))))


; ======= Utils ===========================================

(defn- pull-many-for [db query & inputs]
  (d/pull-many
    db
    '[*]
    (apply d/q query
           db
           inputs)))

(def ^:private transact! (partial d/transact! conn))

(defn- all-of-kind
  "Returns a function that accepts a db (deref'd conn)
   and returns all eids of the given kind"
  [kind]
  (fn [db]
    (pull-many-for db '[:find [?e ...]
                        :in $, ?kind
                        :where [?e :kind ?kind]]
                   kind)))


; ======= Accessors =======================================

(def all-parts (all-of-kind :part))
(def all-products (all-of-kind :product))
(def all-variants (all-of-kind :variant))
(def all-orders (all-of-kind :order))

(defn part-uses-for [db product-id]
  ; NOTE this ignores variants right now
  ; and is mostly just for experimenting with
  ; the query syntax
  (->> (d/q '[:find (pull ?part-use [*]) (pull ?part-id [*])
              :in $, ?product-id
              :where
              [?part-use :part-use/part ?part-id]
              [?variant :variant/parts ?part-use]
              [?variant :variant/product ?product-id]]
            db
            product-id)
       (map (fn [[part-use part]]
              (assoc part :part-use/units
                     (:part-use/units part-use))))))



; ======= Validation ======================================

(defn valid-variant? [v]
  (string? (:name v)))


; ======= Datom creation ==================================

(defn create-part [{:keys [name quantity unit supplier]
                    :or {quantity 0
                         unit "things"
                         supplier ""}
                    :as part}]
  {:pre [(string? name)
         (not (empty? name))]}
  (transact!
    [{:kind :part
      :name name
      :quantity quantity
      :unit unit
      :supplier supplier}]))

(defn create-pending-order
  [{:keys [service-id link buyer-name products]}]
  (transact!
    [{:kind :order
      :service-ids service-id
      :link link
      :buyer-name buyer-name
      :pending? true

      :order/items
      (map
        (fn [p]
          {:kind :order-item
           :order-item/product [:service-ids (:service-id p)]
           :order-item/variants (mapv #(vector :service-ids %)
                                     (:variants p))
           :quantity (:quantity p)})
        products)}]))

(defn- add-service-ids
  "For whatever reason, datascript doesn't seem to like it when
   we include the service-ids list in the map form, so we manually
   add them one at a time."
  [eid service-ids]
  (map
    (fn [id]
      [:db/add eid :service-ids id])
    service-ids))

(defn create-product [{:keys [name variants service-ids]}]
  {:pre [(string? name)
         (every? valid-variant? variants)]}
  (transact!
    (concat
      [{:db/id -1
        :kind :product
        :name name}]

      (add-service-ids -1 service-ids)

      (mapcat (fn [id v]
                (cons
                  (->> {:db/id id
                        :kind :variant
                        :name (:name v)

                        :variant/product -1
                        :variant/default? (boolean
                                            (:default? v))
                        :variant/group (:group v)
                        :variant/parts
                        (map (fn [[part-id units]]
                               {:kind :part-use
                                :part-use/part part-id
                                :part-use/units units})

                             (:parts v))}

                       ; remove nil values
                       (filter second)
                       (into {}))

                  (add-service-ids id (:service-ids v))))

              ; generate temp ids for each variant
              (iterate dec -2)
              variants))))
