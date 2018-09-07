(ns fulfillmint.views
  (:require [re-frame.core :as re-frame]
            [fulfillmint.subs :as subs]
            [fulfillmint.views.error-boundary :refer [error-boundary]]
            [fulfillmint.views.router :refer [router]]

            ; views:
            [fulfillmint.service :as service]
            [fulfillmint.views.home :as home]
            [fulfillmint.views.new-part :as new-part]
            [fulfillmint.views.new-product :as new-product]
            ))

;; main

(def pages
  {:home #'home/home
   :new-part #'new-part/view
   :new-product #'new-product/view
   :service-config #'service/config-view})

(defn main []
  [:<>
   [error-boundary
    [router pages]]])

