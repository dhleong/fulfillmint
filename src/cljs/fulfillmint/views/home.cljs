(ns ^{:author "Daniel Leong"
      :doc "views.home"}
  fulfillmint.views.home
  (:require [reagent.core :as r]
            [fulfillmint.service :as services]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            [fulfillmint.views.new-part :as new-part]
            [fulfillmint.views.new-product :as new-product]
            [fulfillmint.views.widgets :refer [link]]))

(defn existing-user-home []
  [:<>
   [:h4 "Welcome back to Fulfillmint!"]
   [:h5 "Sections"]
   [:ul
    [:li [link {:href "/orders"}
          "Orders"]]
    [:li [link {:href "/parts"}
          "Parts"]]
    [:li [link {:href "/products"}
          "Products"]]]
   [:h5 "Services"]
   [:ul
    (for [{n :name k :key} (services/get-services)]
      ^{:key k}
      [:li [link {:href (str "/services/" (name k) "/config")}
            "Configure " n]])]
   ])

(defn new-user-home [tutorial?]
  [:<>
   [:h4 "Welcome to Fulfillmint!"]

   (cond
     ; step 3
     (seq (<sub [:products]))
     [:<>
      [:p "Awesome, you're all set!"]
      [:p
       [:a {:href "/"
            :on-click (click>reset! tutorial? false)}
        "Click here"]
       " to go to the main dashboard."
       ]]

     ; step 2
     (seq (<sub [:parts]))
     [:<>
      [:p "Great! Now that you have a Part, create a Product that uses it."]
      [:p.info
       "A Product is made up of several Variants, which can be Grouped together
        if it makes sense."]
      [new-product/view]]

     ; step 1
     :else
     [:<>
      [:p "Let's get started by adding a Part."]
      [:p.info
       "A Part, as you might expect, is anything you use to create your Products,
        like a type of bead or wire. Fulfillmint helps you keep track of how many
        Parts you have on hand so you know what you need to get to fulfill orders,
        and when."]

      [new-part/view]])
   ])

(defn home []
  (r/with-let [tutorial? (r/atom (<sub [:new?]))]
    (if @tutorial?
      [new-user-home tutorial?]

      [existing-user-home])))
