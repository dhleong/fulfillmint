(ns ^{:author "Daniel Leong"
      :doc "Abstractions over data store"}
  fulfillmint.data.db
  (:require [clojure.string :as str]
            [datascript.core :as d]
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
    (let [tx (apply f args)]
      (println "-> " tx)
      (d/transact! conn tx))))

; ======= Accessors =======================================

(def all-parts (all-of-kind :part))
(def all-products (all-of-kind :product))
(def all-variants (all-of-kind :variant))
(def all-orders (all-of-kind :order))

(defn variants-for-product [db product-id]
  (pull-many-for
    db
    '[:find [?variant ...]
      :in $, ?product-id
      :where
      [?variant :variant/product ?product-id]]
    product-id))

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

(defn parts-for-orders [db]
  (->> (d/q
         '[:find (pull ?part-id [*]) (sum ?total-parts-used)
           :with ?order-items
           :where
           [?order :order/complete? false]
           [?order :order/pending? false]
           [?order :order/items ?order-items]
           [?order-items :order-item/variants ?variants]
           [?order-items :quantity ?quantity]
           [?variants :variant/parts ?part-use]
           [?part-use :part-use/part ?part-id]
           [?part-use :part-use/units ?part-units]
           [(* ?quantity ?part-units) ?total-parts-used]
           ]
         db)
       (map (fn [[part used]]
              {:db/id (:db/id part)
               :part part
               :needed used}))))

(defn parts-for-product [db product-id]
  (->> (pull-many-for
         db
         '[:find [?part-id ...]
           :in $, ?product-id
           :where
           [?variant :variant/parts ?part-use]
           [?variant :variant/product ?product-id]
           [?part-use :part-use/part ?part-id]]
         product-id)
       (reduce
         (fn [m part]
           (assoc m (:db/id part) part))
         {})))

(defn variants-for-orders [db]
  (->> (d/q
         '[:find (pull ?variants [*]) (pull ?product [*]) (sum ?quantity)
           :with ?order-items
           :where
           [?order :order/complete? false]
           [?order :order/pending? false]
           [?order :order/items ?order-items]
           [?order-items :order-item/variants ?variants]
           [?order-items :quantity ?quantity]
           [?variants :variant/parts ?part-use]
           [?variants :variant/product ?product]
           ]
         db)
       (map (fn [[variant product ordered]]
              {:db/id (:db/id variant)
               :product product
               :variant variant
               :parts (->> variant
                           :variant/parts
                           (map (comp :db/id :part-use/part))
                           (into #{}))
               :ordered ordered}))))

(defn part-uses-for-part [db part-id]
  (->> (d/q
         '[:find (pull ?product [*]) (pull ?variant [*]) ?units
           :in $, ?part-id
           :where
           [?part-use :part-use/part ?part-id]
           [?part-use :part-use/units ?units]
           [?variant :variant/parts ?part-use]
           [?variant :variant/product ?product]
           ]
         db
         part-id)
       (map (fn [[product variant units]]
              {:product product
               :variant variant
               :used units}))))

(defn entity-by-id [db id]
  (d/pull db '[*] id))

(defn product-by-id [db id]
  (let [p (entity-by-id db id)
        variants (variants-for-product db id)]
    (assoc p :variants variants)))


; ======= order queries ===================================

(defn orders-by-product [db product-id]
  (pull-many-for
    db
    '[:find [?order ...]
      :in $, ?product-id
      :where
      [?order :order/items ?order-item]
      [?order :order/complete? false]
      [?order :order/pending? false]
      [?order-item :order-item/product ?product-id]]
    product-id))


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
    :quantity (or quantity 0) ; if nil is passed, :or above fails
    :unit (or unit "things")
    :supplier (or supplier "")}])
(def create-part (transact!-with create-part-tx))

(defn create-order-tx
  [{:keys [service-id link buyer-name products complete? pending?]
    :or {complete? false
         pending? false}}]
  [{:kind :order
    :service-ids service-id
    :link (or link "")
    :buyer-name buyer-name
    :order/complete? (or complete? false)
    :order/pending? (or pending? false)

    :order/items
    (map
      (fn [p]
        {:kind :order-item
         :order-item/product (or (:id p)
                                 [:service-ids (:service-id p)])
         :order-item/variants (mapv #(or (:id %)
                                         [:service-ids %])
                                    (:variants p))
         :quantity (:quantity p 1)})
      products)}])
(def create-order (transact!-with create-order-tx))

(defn- add-service-ids
  "For whatever reason, datascript doesn't seem to like it when
   we include the service-ids list in the map form, so we manually
   add them one at a time."
  [eid service-ids]
  (map
    (fn [id]
      [:db/add eid :service-ids id])
    service-ids))

(defn- upsert-variant-tx
  "Generate the sequence of transact statements
   used to add the variant `v` to the given `product-id`.
   `product-id` may be a lookup ref or a temporary eid.
   If `variant-id` is not provided, the temporary eid `-1`
   will be used for it."
  ([product-id v]
   (upsert-variant-tx product-id -1 v))
  ([product-id variant-id v]
   (let [variant-id (or (:id v)
                        variant-id)]
     (concat
       (when (:id v)
         ; if we're updating a variant, delete any parts before
         ; recreating them below (otherwise, we get extras)
         [[:db.fn/retractAttribute variant-id :variant/parts]])

       [(->> {:db/id variant-id
              :kind :variant
              :name (:name v)

              :variant/product product-id
              :variant/default? (boolean
                                  (:default? v))
              :variant/group (let [g (:group v)]
                               (when-not (str/blank? g)
                                 g))
              :variant/parts
              (->> v
                   :parts
                   (filter second)
                   (map (fn [[part-id units]]
                          {:kind :part-use
                           :part-use/part {:db/id part-id}
                           :part-use/units units})))}

             ; remove nil values
             (filter second)
             (into {}))]

       (add-service-ids variant-id (:service-ids v))))))
(def create-variant (transact!-with upsert-variant-tx))

(defn upsert-product-tx [{:keys [name variants service-ids] :as p}]
  {:pre [(string? name)
         (every? valid-variant? variants)]}
  (let [product-id (:id p -1)] ; if :id is present, upsert
    (concat
      [{:db/id product-id
        :kind :product
        :name name}]

      (add-service-ids product-id service-ids)

      (mapcat
        (fn [variant-temp-id v]
          (upsert-variant-tx product-id variant-temp-id v))

        ; generate temp ids for each variant
        (iterate dec -2)
        variants))))
(def upsert-product (transact!-with upsert-product-tx))
