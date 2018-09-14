(ns ^{:author "Daniel Leong"
      :doc "reports.parts-for-orders"}
  fulfillmint.views.reports.parts-for-orders
  (:require [fulfillmint.util :refer [<sub]]))

(defn view []
  (let [parts (<sub [:parts-needed-for-orders])]
    [:<>
     [:h4 "Parts needed for Orders"]
     [:table.parts
      [:tbody
       [:tr
        [:th "Part"]
        [:th "Available"]
        [:th "Total needed"]]

       (for [{:keys [id part needed available]} parts]
         ^{:key id}
         [:tr
          [:td (:name part)]
          [:td available]
          [:td needed]])]]]))
