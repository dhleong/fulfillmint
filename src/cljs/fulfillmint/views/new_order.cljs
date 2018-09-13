(ns ^{:author "Daniel Leong"
      :doc "new-order"}
  fulfillmint.views.new-order
  (:require-macros [fulfillmint.util.log :as log :refer [log]])
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.service.util :refer [->service-id]]
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

(defn clean-order [f]
  (let [service-id-raw (:id (::service-id f))]
    (-> f
        (dissoc ::service-id)
        (update :products (partial
                            map
                            (fn [p]
                              (-> p
                                  (dissoc :product)
                                  (assoc :id (:id (:product p)))
                                  (update :variants vals)))))

        (cond->
          ; insert a parsed :service-id if possible
          (not (str/blank? service-id-raw))
          (assoc :service-id
                 (->service-id
                   (:service (::service-id f))
                   service-id-raw))))))

(defn submit-form [just-added f]
  (let [f (clean-order f)
        _ (println f)

        invalid? (or
                   (not (:service-id f))
                   (empty? (:products f))
                   (str/blank? (get f :buyer-name)))]
    (when-not invalid?
      (log "SUBMIT " f)
      (>evt [:create-order f])
      (reset! just-added f))))

(defn view []
  (r/with-let [form (r/atom {:products []})
               just-added (r/atom nil)]
    [:<>
     [:h4 "New Order"]

     (when-some [added @just-added]
       [:p "Just added: " (:service-id added)])

     [:form {:on-submit (fn-click
                          (submit-form just-added @form))}
      [bind-fields
       [:<>
        (service-id-entry {:id [::service-id]
                           :placeholder "Order ID"
                           :validator (fn [{:keys [id service]}]
                                        (when (str/blank? id)
                                          ["error"]))})

        [:div.form-part
         [:input {:field :text
                  :id :link
                  :placeholder "Link (optional)"}]]

        [:div.form-part
         [:input {:field :text
                  :id :buyer-name
                  :placeholder "Buyer name"
                  :validator (fn [n]
                               (when (str/blank? n)
                                 ["error"]))}]]]

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
