(ns ^{:author "Daniel Leong"
      :doc "views.parts"}
  fulfillmint.views.parts
  (:require [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]))

(defn view []
  [:<>
   [:h4 "Parts"]
   [:div.new
    [link {:href "/parts/new"}
     "Create a new part"]]
   [searchable
    :sub [:parts]
    :->url (fn [p]
             (str "/parts/" (:id p)))]])

