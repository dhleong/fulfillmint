(ns ^{:author "Daniel Leong"
      :doc "ordered-variants"}
  fulfillmint.views.reports.ordered-variants
  (:require [fulfillmint.util :refer [<sub]]
            [fulfillmint.views.widgets :refer [link]]))

(defn view []
  (let [variants (<sub [:ordered-variants])]
    [:<>
     [:h4 "Ordered Product Variants"]
     [:table.variants
      [:tbody
       [:tr
        [:th "Product"]
        [:th "Variant"]
        [:th "# Ordered"]]

       (for [{:keys [id variant ordered product]} variants]
         ^{:key id}
         [:tr
          [:td
           [link {:href (str "/product/" (:id product))}
            (:name product)]]
          [:td
           (:name variant)]
          [:td
           ordered]]) ]]
     ]))
