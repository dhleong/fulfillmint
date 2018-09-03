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

  (defroute "/products" []
    (navigate! :products))

  (defroute "/products/new" []
    (navigate! :product-builder))

  (defroute "/products/:id" [id]
    (navigate! :product (keyword id)))

  (defroute "/providers/:provider-id/config" [provider-id]
    (navigate! :provider-config (keyword provider-id)))

  ;; --------------------
  (hook-browser-navigation!))

