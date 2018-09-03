(ns ^{:author "Daniel Leong"
      :doc "Data storage / query"}
  fulfillmint.data
  (:require [fulfillmint.data.db]))

;; NOTE:
;; Fulfillment data is stored in a DB format that is opaque to other
;; UI code. Currently we use datascript to handle queries and storage,
;; but that could change. The data ns is responsible for creating
;; re-frame subscriptions and events that act as abstractions on top
;; of the underlying storage format

;; Subscriptions:
;;
;; [:products]
;; all registered products, as {:id,:name}
;;
;; [:product <id>]
;; details of a specific product by its id
;;
;; [:variants <product-id>]
;; all "variants" of a given product, each as {:id,:name,:parts}
;; where :parts is a mapping of <part-id> -> quantity.
;;
;; [:parts]
;; all registered parts, each as {:id,:name,:quantity,:unit,:supplier}
;;
;; [:orders]
;; all known orders, each as {:id,:buyer-name,:service,:service-id,:link,:variants,:done?}
;;
;; [:pending-orders]
;; newly-imported orders from a service, each as {:id,:buyer-name,:service,:service-id,:link}


;; Events:
;;
;; [:create-part {:id,:name,:quantity,:unit,:supplier}]
;;
;; [:create-product {:name,:variants}] where variants is
;; a collection of {:name,:default?,:group,:parts,:service-ids} and
;; :parts is map of <part-id> -> units
;;
;; [:create-variant <product-id> {:parts}]
;;
;; [:create-pending-order {:service,:service-id,:link,:buyer-name,:products}]
;; where :products is a sequence of:
;;  {:id <service-id>
;;   :variants [<variant-service-id>...]
;;   :quantity <number>}
