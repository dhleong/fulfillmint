(ns ^{:author "Daniel Leong"
      :doc "widgets.service-id-entry"}
  fulfillmint.views.widgets.service-id-entry
  (:require [fulfillmint.service :as services]))

(defn- service-id-entry [{:keys [id placeholder]}]
  [:div.form-part
   [:select {:field :list
             :id (conj id :service)}
    (for [{n :name k :key} (services/get-services)]
      [:option {:key k} n])]
   [:input {:field :text
            :id (conj id :id)
            :autoComplete 'off
            :placeholder placeholder}]])

