(ns ^{:author "Daniel Leong"
      :doc "new-product"}
  fulfillmint.views.new-product
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.service.util :refer [->service-id]]
            [fulfillmint.util :refer [<sub >evt vec-dissoc]
             :refer-macros [fn-click]]
            [fulfillmint.views.widgets :refer-macros [icon]]
            ))

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

(defn- service-ids-entry [id]
  [:div.form-part
   [:select {:field :list
             :id (conj id :service)}
    ; TODO build dynamically from services
    [:option {:key :etsy} "Etsy"]]
   [:input {:field :text
            :id (conj id :id)
            :autoComplete false
            :placeholder "ID or Serial Number"}]])

(defn- service-ids-form [form kind path]
  (let [entries (->> (get-in @form path)
                     (filter :name)
                     count)]
    [:div.form-part
     [:div.desc "Is this sold on any services?"]

     (for [i (range (inc entries))]
       ^{:key i}
       [bind-fields
        (service-ids-entry (conj path i))
        form])

     ]))

(defn- part-form [form path]
  [:div.part-entry
   [bind-fields
    [:div.form-part {:field :typeahead
                     :id (conj path :part)
                     :data-source (fn [input]
                                    (let [match (str/lower-case input)]
                                      (->> (<sub [:parts])
                                           (filter
                                             #(str/includes? (str/lower-case
                                                               (:name %))
                                                             match))
                                           (map #(vector (:name %) %)))))
                     :in-fn :name
                     :out-fn second
                     :result-fn first}]
    form]

   (when-let [p (get-in @form (conj path :part))]
     [bind-fields
      [:div.form-part
       [:input {:field :numeric
                :id (conj path :quantity)
                :placeholder (str "# " (:unit p) " per product")}]]
      form])])

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
                          (keep :part)
                          count)]
     [:div
      "Add parts:"
      (for [i (range (inc parts-count))]
        ^{:key i}
        [part-form form [:variants index ::parts i]])])

   [service-ids-form form :variant [:variants index ::services]]
   ])

(defn view []
  (r/with-let [form (r/atom {::services []
                             :variants [{::services []}]})
               just-added (r/atom nil)]
    [:<>
     [:h5 "New Product"]

     (when-some [added @just-added]
       [:p "Just added: " (:name added)])

     [:form {:on-submit (fn-click
                          (let [product @form]
                            (println (clean-product product))
                            (when-not (empty? (:name product))
                              (let [cleaned (clean-product product)]
                                (>evt [:create-product cleaned])
                                (reset! just-added cleaned)))))}
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
                :value "Create Product"}]]]
     ]))
