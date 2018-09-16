(ns ^{:author "Daniel Leong"
      :doc "Selling Service integration / abstraction"}
  fulfillmint.service
  (:require [fulfillmint.service.etsy.config :as etsy-config]
            [fulfillmint.service.util :refer [unpack-service-id]]))

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

(defn describe-service-id [id]
  (let [[service raw-id] (unpack-service-id id)
        service-name (get-in services [service :name])]
    (str raw-id " (" service-name ")")))
