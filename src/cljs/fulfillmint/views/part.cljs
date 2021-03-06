(ns ^{:author "Daniel Leong"
      :doc "part"}
  fulfillmint.views.part
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]
            [fulfillmint.views.widgets.fast-numeric]))

(defn view [id]
  (let [part (<sub [:part id])
        variant->orders (<sub [:ordered-variants-for-part id])]
    [:<>
     [:h4 "Part #" id ": " (:name part)]

     [:h5 "Available"]
     [bind-fields
      [:div [:input {:field :fast-numeric
                     :id :quantity} ]
       " " (:unit part)]
      {:get #(:quantity (<sub [:part id]))
       :save! #(>evt [:set-part-quantity id %2])}]

     [:h5 "Uses"]
     [:table.makeable-variants
      [:tbody
       [:tr
        [:th "Product"]
        [:th "Variant"]
        [:th (str/capitalize (:unit part)) " / item"]
        [:th "# items makeable"]
        [:th "# items ordered"]]

       (for [{:keys [product variant used]} (<sub [:part-uses-for-part id])]
         (let [makeable (Math/floor (/ (:quantity part) used))]
           ^{:key (:id variant)}
           [:tr.part-use
            [:td
             [link {:href (str "/products/" (:id product))}
              (:name product)]]
            [:td (:name variant)]
            [:td used]

            ; TODO base class on # variants actually ordered
            [:td {:class (case makeable
                           0 "none-makeable"
                           (1 2) "few-makeable"
                           nil)}
             makeable]
            [:td (get variant->orders (:id variant))]]))]]
     ]))
