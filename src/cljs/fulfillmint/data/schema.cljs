(ns ^{:author "Daniel Leong"
      :doc "schema"}
  fulfillmint.data.schema)

; shared attrs
(def shared-schema
  {:name {:db/cardinality :db.cardinality/one
          :db/doc "Something's name"}
   :service-ids {:db/cardinality :db.cardinality/many
                 :db/unique :db.unique/identity
                 :db/doc "Something's service IDs (it might have more than one)"}
   :kind {:db/cardinality :db.cardinality/one
          :db/doc "The kind of entity"}
   })

(def order-schema
  {:order/items {:db/valueType :db.type/ref
                 :db/isComponent true
                 :db/cardinality :db.cardinality/many
                 :db/doc "The items in this order"}})

; component of :order/items
(def order-item-schema
  {:order-item/product {:db/valueType :db.type/ref
                        :db/cardinality :db.cardinality/one
                        :db/doc "The product an order item is for"}
   :order-item/variants {:db/valueType :db.type/ref
                         :db/cardinality :db.cardinality/many
                         :db/doc "The product an order item is for"}})

; component of :variant/parts
(def part-use-schema
  {:part-use/part {:db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/one
                   :db/doc "The Part this part-use is for"}
   :part-use/units {:db/cardinality :db.cardinality/one
                    :db/doc "How many units of the Part this Variant uses"}
   })

(def variant-schema
  {:variant/product {:db/valueType :db.type/ref
                     :db/cardinality :db.cardinality/one
                     :db/index true
                     :db/doc "The product a variant is for"}

   :variant/default? {:db/cardinality :db.cardinality/one
                      :db/doc "Whether this variant is chosen by default"}

   :variant/group {:db/cardinality :db.cardinality/one
                   :db/doc "The variant group this is associated with (if any)"}

   :variant/parts {:db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/many
                   :db/isComponent true
                   :db/doc "Ref to :part-use items for a variant"}})

(def schema (merge order-schema
                   order-item-schema
                   part-use-schema
                   shared-schema
                   variant-schema))
