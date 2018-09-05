(ns ^{:author "Daniel Leong"
      :doc "new-part"}
  fulfillmint.views.new-part
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.util :refer [<sub >evt]
             :refer-macros [fn-click]]))

(defn view []
  (r/with-let [form (r/atom {})
               just-added (r/atom nil)]
    [:<>
     [:h5 "New Part"]

     (when-some [added @just-added]
       [:p "Just added: " (:name added)])

     [bind-fields
      [:form {:on-submit (fn-click
                           (let [part @form]
                             (when-not (empty? (:name part))
                               (>evt [:create-part part])
                               (reset! just-added part))))}
       ; TODO validate:
       [:div [:input {:field :text
                    :id :name
                    :autoComplete 'off
                    :placeholder "Part Name"}]]

       [:div
        [:div "Do you have any on hand? (optional)"]
        [:input {:field :numeric
                 :id :quantity
                 :placeholder "Count"}]
        [:input {:field :text
                 :id :unit
                 :placeholder "Unit (eg: feet)"}]]

       [:div
        [:div "Where do you get this? (optional)"]
        [:input {:field :text
                 :id :supplier
                 :placeholder "Supplier"}]]

       [:div
        [:input {:type :submit
                 :value "Create Part!"}]]
       ]
      form]
     ]))
