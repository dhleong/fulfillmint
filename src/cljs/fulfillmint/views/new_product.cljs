(ns ^{:author "Daniel Leong"
      :doc "new-product"}
  fulfillmint.views.new-product
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.service :as services]
            [fulfillmint.service.util :refer [->service-id
                                              unpack-service-id]]
            [fulfillmint.util :refer [<sub >evt vec-dissoc]
             :refer-macros [fn-click]]
            [fulfillmint.views.widgets :refer-macros [icon]]
            [fulfillmint.views.widgets.fast-numeric]
            [fulfillmint.views.widgets.typeahead :refer [typeahead]]
            [fulfillmint.views.widgets.service-id-entry
             :refer [service-id-entry]]))

; ======= form -> product =================================

(defn- update-service-ids
  [entity]
  (-> entity
      (dissoc ::services)
      (assoc :service-ids
             (->> entity
                  ::services
                  (filter :id)
                  (map #(->service-id (:service %)
                                      (:id %)))
                  seq))))

(defn clean-variant [variant]
  (-> variant
      (update-service-ids)
      (dissoc ::parts)
      (assoc :parts
             (->> variant
                  ::parts
                  vals
                  (filter :part)
                  (map (fn [{:keys [part quantity]}]
                         [(:id part) quantity]))
                  (into {})))))

(defn clean-product [product]
  (-> product
      (update-service-ids)
      (update :variants (partial map clean-variant))
      ))


; ======= product -> form =================================

(defn- service-ids->form [entity]
  (-> entity
      (dissoc :service-ids)
      (assoc ::services
             (mapv
               (fn [id]
                 (let [[service raw-id] (unpack-service-id id)]
                   {:service service :id (int raw-id)}))
               (:service-ids entity)))))

(defn- variant->form [parts-by-id v]
  (-> v
      (service-ids->form)
      (dissoc :kind)
      (dissoc :variant/product)

      (dissoc :variant/group)
      (assoc :group (:variant/group v))

      (dissoc :variant/parts)
      (assoc ::parts (reduce (fn [m {:part-use/keys [part units]}]
                               (assoc m
                                      (count m)
                                      {:part (get parts-by-id (:db/id part))
                                       :quantity units}))
                             {}
                             (:variant/parts v)))))

(defn product->form [parts-by-id product]
  (-> product
      (service-ids->form)
      (dissoc :kind)
      (update :variants (partial mapv (partial variant->form parts-by-id)))))


; ======= UI ==============================================

(defn- service-ids-form [form kind path]
  (let [entries (->> (get-in @form path)
                     (filter :name)
                     count)]
    [:div.form-part
     [:div.desc "Is this sold on any services?"]

     (for [i (range (inc entries))]
       ^{:key i}
       [bind-fields
        (service-id-entry {:id (conj path i)
                           :placeholder "ID or Serial Number"})
        form])

     ]))

(defn- part-form [form path]
  [:div.part-entry
   [bind-fields
    (typeahead
      :id (conj path :part)
      :placeholder "Part Name"
      :query-sub [:parts])
    form]

   (when-let [p (get-in @form (conj path :part))]
     (when (:name p)
       [bind-fields
        [:div.form-part
         [:input {:field :fast-numeric
                  :id (conj path :quantity)
                  :placeholder (str "# " (:unit p) " per product")}]]
        form]))])

(defn- variant-form [form index]
  [:<>
   [:h5
    "Variant #" (inc index)
    (when (> index 0)
      [:a {:href "#"
           :on-click (fn-click
                       (swap! form update :variants vec-dissoc index))}
       (icon :close)])]

   [bind-fields
    [:<>
     [:div.form-part
      [:input {:field :text
               :id [:variants index :name]
               :placeholder "Variant Name"}]]
     [:div.form-part
      [:input {:field :text
               :id [:variants index :group]
               :placeholder "Group (optional)"}]]
     [:div.form-part
      [:input {:field :checkbox
               :id [:variants index :default?]}]
      [:label {:for [:variants index :default?]}
       "Apply this variation by default"]]]
    form]

   (let [parts-count (->> (get-in @form [:variants index ::parts])
                          vals
                          (keep (comp :name :part))
                          count)]
     [:div
      "Add parts:"
      (for [i (range (inc parts-count))]
        ^{:key i}
        [part-form form [:variants index ::parts i]])])

   [service-ids-form form :variant [:variants index ::services]]
   ])


; ======= Shared product editing form =====================

(defn product-edit-form
  [& {:keys [initial-form submit-label on-submit]}]
  (r/with-let [form (r/atom initial-form)]
    [:form {:on-submit (fn-click
                         (let [product @form]
                           (println product)
                           (when-not (empty? (:name product))
                             (on-submit (clean-product product)))))}
     [bind-fields
      [:<>
       ; TODO validate:
       [:div.form-part
        [:input {:field :text
                 :id :name
                 :autoComplete 'off
                 :placeholder "Product Name"}]]]
      form]

     [service-ids-form form :product [::services]]

     ;; (println "Variants:" (:variants @form))
     (for [i (range (count (:variants @form)))]
       ^{:key i}
       [variant-form form i])

     [:div.form-part
      [:a {:href "#"
           :on-click (fn-click
                       (swap! form update :variants conj
                              {::services []}))}
       "Add another Variant"]]

     [:div.form-part
      [:input {:type :submit
               :value submit-label}]]]
    ))


; ======= public view =====================================

(defn view []
  (r/with-let [just-added (r/atom nil)]
     [:<>

      [:h5 "New Product"]

      (when-some [added @just-added]
        [:p "Just added: " (:name added)])

      [product-edit-form
       :initial-form {::services []
                      :variants [{::services []}]}
       :submit-label "Create Product"
       :on-submit (fn on-submit [product]
                    (println product)
                    (>evt [:create-product product])
                    (reset! just-added product))]]))
