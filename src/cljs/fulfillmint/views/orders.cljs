(ns ^{:author "Daniel Leong"
      :doc "views.orders"}
  fulfillmint.views.orders
  (:require [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]))

(defn view []
  [:<>
   [:h4 "Orders"]
   [:div.new
    [link {:href "/orders/new"}
     "Record a new Order"]]
   [searchable
    :sub [:orders]
    :->url (fn [p]
             (str "/orders/" (:id p)))]])
