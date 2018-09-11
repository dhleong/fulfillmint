(ns fulfillmint.views
  (:require [re-frame.core :as re-frame]
            [fulfillmint.subs :as subs]
            [fulfillmint.views.error-boundary :refer [error-boundary]]
            [fulfillmint.views.router :refer [router]]

            ; views:
            [fulfillmint.service :as service]
            [fulfillmint.views.home :as home]
            [fulfillmint.views.order :as order]
            [fulfillmint.views.orders :as orders]
            [fulfillmint.views.new-order :as new-order]
            [fulfillmint.views.part :as part]
            [fulfillmint.views.parts :as parts]
            [fulfillmint.views.new-part :as new-part]
            [fulfillmint.views.product :as product]
            [fulfillmint.views.products :as products]
            [fulfillmint.views.new-product :as new-product]
            ))

;; main

(def pages
  {:home #'home/home
   :order #'order/view
   :orders #'orders/view
   :new-order #'new-order/view
   :part #'part/view
   :parts #'parts/view
   :new-part #'new-part/view
   :product #'product/view
   :products #'products/view
   :new-product #'new-product/view
   :service-config #'service/config-view})

(defn main []
  [:<>
   [error-boundary
    [router pages]]])

