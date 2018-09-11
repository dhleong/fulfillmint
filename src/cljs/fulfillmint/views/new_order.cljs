(ns ^{:author "Daniel Leong"
      :doc "new-order"}
  fulfillmint.views.new-order
  (:require [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.widgets :refer [link]]))

(defn view []
  [:<>
   [:h4 "New Order"]
   "TK"])
