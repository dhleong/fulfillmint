(ns ^{:author "Daniel Leong"
      :doc "views.router"}
  fulfillmint.views.router
  (:require [fulfillmint.util :refer [<sub >evt]]))

(defn footer [])  ;; placeholder

(defn- has-footer? [page] false) ;; for now

(defn router
  "Renders the current page, given a map
   of page-id to page render fn."
  [routes-map]
  (let [[page args] (<sub [:page])
        page-form [(get routes-map page) args]]
    (println "[router]" page args)

    (if (has-footer? page)
      ; we have to do a bit of wrapping to render the footer nicely
      [:div#footer-container
       [:div.content
        page-form]
       [footer]]

      ; no footer; just render the page directly
      page-form)))


