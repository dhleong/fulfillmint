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

  (defroute "/parts" []
    (navigate! :parts))

  (defroute "/parts/new" []
    (navigate! :new-part))

  (defroute "/parts/:id" [id]
    (navigate! :part id))

  (defroute "/products/new" []
    (navigate! :new-product))

  (defroute "/products/:id" [id]
    (navigate! :product id))

  (defroute "/providers/:provider-id/config" [provider-id]
    (navigate! :provider-config (keyword provider-id)))

  ;; --------------------
  (hook-browser-navigation!))

