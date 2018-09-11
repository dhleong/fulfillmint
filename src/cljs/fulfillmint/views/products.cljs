(ns ^{:author "Daniel Leong"
      :doc "views.products"}
  fulfillmint.views.products
  (:require [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]))

(defn view []
  [:<>
   [:h4 "Products"]
   [:div.new
    [link {:href "/products/new"}
     "Create a new Product"]]
   [searchable
    :sub [:products]
    :->url (fn [p]
             (str "/products/" (:id p)))]])


