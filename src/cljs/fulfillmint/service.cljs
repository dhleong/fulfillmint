(ns ^{:author "Daniel Leong"
      :doc "Selling Service integration / abstraction"}
  fulfillmint.service
  (:require [fulfillmint.service.etsy.config :as etsy-config]))

(def ^:private services
  {:etsy
   {:key :etsy
    :name "Etsy"
    :config etsy-config/view}})

(defn get-info
  [service-key]
  (get services service-key))

(defn get-services []
  (->> (vals services)
       (sort-by :name)))

(defn- ->val
  "Convenience for use with if-let; gets the value of the
   given key `k` for the given `service-key`."
  [service-key k]
  (some->> service-key
           get-info
           k))

(defn config-view
  [service-key]
  (if-let [config (->val service-key :config)]
    [config]

    [:div.error "No config for this provider"]))
