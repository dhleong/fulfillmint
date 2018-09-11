(ns ^{:author "Daniel Leong"
      :doc "order"}
  fulfillmint.views.order
  (:require [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]))

(defn view [id]
  [:div "Order " id])


