(ns ^{:author "Daniel Leong"
      :doc "new-order"}
  fulfillmint.views.new-order
  (:require-macros [fulfillmint.util.log :as log :refer [log]])
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.widgets :refer [link]]
            [fulfillmint.views.widgets.typeahead :refer [typeahead]]
            [fulfillmint.views.widgets.service-id-entry
             :refer [service-id-entry]]))

(defn- product-variant-form [form path product]
  [:div.variant-part
   [bind-fields
    (typeahead
      :id path
      :placeholder "Variant name"
      :query-sub [:variants-of product])
    form]])

(defn- product-form [form path]
  [:div.product-entry
   [bind-fields
    (typeahead
      :id (conj path :product)
      :placeholder "Product Name"
      :query-sub [:products])
    form]

   (when-let [p (get-in @form (conj path :product))]
     (when-not (string? p)
       (let [path (conj path :variants)]
         [:<>
          [:i "Variants"]
          (for [i (range (->> (get-in @form path)
                              vals
                              (keep :name)
                              count
                              inc))]
            ^{:key i}
            [product-variant-form form (conj path i) p])
          [:hr]])))])

(defn view []
  (r/with-let [form (r/atom {:products []})
               just-added (r/atom nil)]
    [:<>
     [:h4 "New Order"]

     (when-some [added @just-added]
       [:p "Just added: " (:service-id added)])

     [:form {:on-submit (fn-click
                          (log @form))}
      [bind-fields
       [:<>
        (service-id-entry {:id [::service-id]
                           :placeholder "Order ID"})

        [:div.form-part
         [:input {:field :text
                  :id :link
                  :placeholder "Link (optional)"}]]

        [:div.form-part
         [:input {:field :text
                  :id :buyer-name
                  :placeholder "Buyer name"}]]]

       form]

      [:h5 "Ordered Products"]

      (for [i (range (->> (:products @form)
                          (keep (comp :name :product))
                          count
                          inc))]
        ^{:key i}
        [product-form form [:products i]])

      [:div.form-part
       [:input {:type :submit
                :value "Record Order"}]]]

     ]))
