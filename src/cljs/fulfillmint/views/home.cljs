(ns ^{:author "Daniel Leong"
      :doc "views.home"}
  fulfillmint.views.home
  (:require [fulfillmint.util :refer [<sub >evt fn-click]]))

(defn home []
  (println "HOME")
  (let (seq (<sub [:parts]))
    (println "Parts:" parts)
    [:div
     [:h4 "Welcome to Fulfillment"]
     [:a {:href "#"
          :on-click (fn-click
                      (>evt [:create-part {:name (str "Part-"
                                                      (js/Date.now))}]))}
      "Create Part"]
     (if (seq parts)
       [:<>
        (println "Part" (first parts))
        (for [p parts]
          ^{:key (:name p)}
          [:div (str p)])]

       [:div "No parts"])]))
