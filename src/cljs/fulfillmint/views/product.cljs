(ns ^{:author "Daniel Leong"
      :doc "product"}
  fulfillmint.views.product
  (:require [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.new-product :refer [product->form product-edit-form]]
            [fulfillmint.views.searchable :refer [searchable]]
            [fulfillmint.views.widgets :refer [link]]))

(defn edit-form [parts-by-id product]
  (r/with-let [last-save (r/atom nil)]
    [:<>

     [:h5 "Edit Product"]

     (when-let [s @last-save]
       [:p "Saved: " (str s)])

     (println "Edit" product)
     [product-edit-form
      :initial-form (product->form parts-by-id product)
      :submit-label "Update Product"
      :on-submit (fn on-submit [p]
                   (println "Save " p)
                   (>evt [:update-product p])
                   (reset! last-save (js/Date.)))]]))

(defn view [id]
  (let [product (<sub [:product id])
        parts-by-id (<sub [:parts-for-product id])]
    (println "editing: " product)
    [:<>
     [:h4 "Product #" id ": " (:name product)]
     [:h5
      "Pending Orders: "
      (count (<sub [:orders-by-product (:id product)]))]

     [edit-form parts-by-id product]
     ]))

