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

(defn- all-of-kind
  "Returns a function that accepts a db (deref'd conn)
   and returns all eids of the given kind"
  [kind]
  (fn [db]
    (pull-many-for db '[:find [?e ...]
                        :in $, ?kind
                        :where [?e :kind ?kind]]
                   kind)))

(defn- transact!-with
  "Create a function that accepts a `conn` and some args, passing
   the args to `f` and calling `(transact!)` on the conn
   and the result of `(f args)`"
  [f]
  (fn transactor [conn & args]
    (println "transact! " f args)
    (d/transact! conn (apply f args))))

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

(defn create-part-tx [{:keys [name quantity unit supplier]
                       :or {quantity 0
                            unit "things"
                            supplier ""}
                       :as part}]
  {:pre [(string? name)
         (not (empty? name))]}
  [{:kind :part
    :name name
    :quantity quantity
    :unit unit
    :supplier supplier}])
(def create-part (transact!-with create-part-tx))

(defn create-pending-order-tx
  [{:keys [service-id link buyer-name products]}]
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
      products)}])
(def create-pending-order (transact!-with create-pending-order-tx))

(defn- add-service-ids
  "For whatever reason, datascript doesn't seem to like it when
   we include the service-ids list in the map form, so we manually
   add them one at a time."
  [eid service-ids]
  (map
    (fn [id]
      [:db/add eid :service-ids id])
    service-ids))

(defn- create-variant-tx
  "Generate the sequence of transact statements
   used to add the variant `v` to the given `product-id`.
   `product-id` may be a lookup ref or a temporary eid.
   If `variant-id` is not provided, the temporary eid `-1`
   will be used for it."
  ([product-id v]
   (create-variant-tx product-id -1 v))
  ([product-id variant-id v]
   (cons
     (->> {:db/id variant-id
           :kind :variant
           :name (:name v)

           :variant/product product-id
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

     (add-service-ids variant-id (:service-ids v)))))
(def create-variant (transact!-with create-variant-tx))

(defn create-product-tx [{:keys [name variants service-ids]}]
  {:pre [(string? name)
         (every? valid-variant? variants)]}
  (concat
    [{:db/id -1
      :kind :product
      :name name}]

    (add-service-ids -1 service-ids)

    (mapcat
      (fn [id v]
        (create-variant-tx -1 id v))

      ; generate temp ids for each variant
      (iterate dec -2)
      variants)))
(def create-product (transact!-with create-product-tx))
