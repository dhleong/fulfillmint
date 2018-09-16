(ns ^{:author "Daniel Leong"
      :doc "order"}
  fulfillmint.views.order
  (:require [reagent.core :as r]
            [fulfillmint.service :refer [describe-service-id]]
            [fulfillmint.util :refer [<sub >evt click>evt click>reset! fn-click]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]))

(defn pending-order-editor [o]
  [:<>
   [:div "TODO: PENDING ORDER"]
   [:div (str o)]])

(defn order-viewer [o]
  [:<>
   [:div.order-completion
    (if (:order/complete? o)
      "Completed!"
      [:input {:type :button
               :value "Mark Complete & Consume Parts"
               :on-click (click>evt [:mark-order-complete! o])}])]

   [:div.items
    (for [item (:items o)]
      ^{:key (:id item)}
      [:div.item
       [:div.quantity (:quantity item)]
       [:div.item-info

        [:div.product
         [link {:href (str "/products/" (-> item :product :id))}
          (-> item :product :name)]]

        [:ul.variants
         (for [v (:variants item)]
           ^{:key (:id v)}
           [:li.variant
            [:div.name (:name v)]])]]

       ; this is a bit gross:
       [:div.parts
        [:i "Parts used:"]
        [:ul.parts
         (for [p (mapcat :variant/parts (:variants item))]
           ^{:key (-> p :part :id)}
           [:li.part
            [:div.name
             [link {:href (str "/parts/" (-> p :part :id))}
              (-> p :part :name)]]
            [:div.use
             (-> p :part-use/units)
             " / "
             (-> p :part :quantity)]])]]
       ])]
   ])

(defn view [id]
  (if-let [o (<sub [:order id])]
    (let [title (str "Order " (describe-service-id (:service-id o)))]
      [:div.order-view
       [:h4 (if-let [link (:link o)]
              [:a {:href link
                   :target '_blank}
               title]
              title)]

       [:div.info
        [:div.buyer (:buyer-name o)]]

       (if (:order/pending? o)
         [pending-order-editor o]
         [order-viewer o])
       ])

    [:div "No such order"]))


