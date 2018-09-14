(ns ^{:author "Daniel Leong"
      :doc "reports"}
  fulfillmint.views.reports
  (:require [fulfillmint.views.reports.parts-for-orders :as parts-for-orders]
            [fulfillmint.views.widgets :refer [link]]))

(def reports
  {:parts-for-orders
   {:name "Parts needed for Orders"
    :view parts-for-orders/view}})

(defn links []
  [:ul
   (for [[k r] reports]
     ^{:key k}
     [:li [link {:href (str "/reports/" (name k))}
           (:name r)]])])

(defn router [report-id]
  (if-let [view-fn (get-in reports [report-id :view])]
    [view-fn]
    [:div "No such report..."]))

(defn view []
  [:<>
   [:h4 "Fulfillmint Reports"]
   [:p "Select one below:"]
   [links]])
