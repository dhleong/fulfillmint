(ns ^{:author "Daniel Leong"
      :doc "new-product"}
  fulfillmint.views.new-product
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.util :refer [<sub >evt vec-dissoc]
             :refer-macros [fn-click]]
            [fulfillmint.views.widgets :refer-macros [icon]]
            ))

(defn- variant-form [form index]
  [:<>
   [:h5
    "Variant #" (inc index)
    (when (> index 0)
      [:a {:href "#"
           :on-click (fn-click
                       (swap! form update ::variants vec-dissoc index))}
       (icon :close)])]

   [bind-fields
    [:<>
     [:div
      [:input {:field :text
               :id [::variants index :name]
               :placeholder "Variant Name"}]]]
    form]])

(defn view []
  (r/with-let [form (r/atom {::variants [{}]})
               just-added (r/atom nil)]
    [:<>
     [:h5 "New Product"]

     (when-some [added @just-added]
       [:p "Just added: " (:name added)])

     [:form {:on-submit (fn-click
                          (let [product @form]
                            (println product)
                            (when-not (empty? (:name product))
                              (>evt [:create-product product])
                              (reset! just-added product))))}
      [bind-fields
       [:<>
        ; TODO validate:
        [:div [:input {:field :text
                       :id :name
                       :autoComplete 'off
                       :placeholder "Product Name"}]]]

       form]

      (for [i (range (count (::variants @form)))]
        ^{:key i}
        [variant-form form i])

      [:div
       [:a {:href "#"
            :on-click (fn-click
                        (swap! form update ::variants conj {}))}
        "Add another Variant"]]

      [:div
       [:input {:type :submit
                :value "Create Product"}]]]
     ]))
