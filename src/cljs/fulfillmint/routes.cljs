(ns fulfillmint.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:require [fulfillmint.util.nav :as nav :refer [hook-browser-navigation!
                                                  navigate!]]))

(defn app-routes []
  (nav/init!)

  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (navigate! :home))

  ; orders:
  (defroute "/orders" []
    (navigate! :orders))

  (defroute "/orders/new" []
    (navigate! :new-order))

  (defroute "/orders/:id" [id]
    (navigate! :order id))

  ; parts
  (defroute "/parts" []
    (navigate! :parts))

  (defroute "/parts/new" []
    (navigate! :new-part))

  (defroute "/parts/:id" [id]
    (navigate! :part id))

  ; products
  (defroute "/products" []
    (navigate! :products))

  (defroute "/products/new" []
    (navigate! :new-product))

  (defroute "/products/:id" [id]
    (navigate! :product id))

  ; services
  (defroute "/services/:service-key/config" [service-key]
    (navigate! :service-config (keyword service-key)))

  ;; --------------------
  (hook-browser-navigation!))

