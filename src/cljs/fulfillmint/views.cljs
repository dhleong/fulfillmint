(ns fulfillmint.views
  (:require [re-frame.core :as re-frame]
            [fulfillmint.subs :as subs]
            [fulfillmint.views.router :refer [router]]

            ; views:
            [fulfillmint.views.home :as home]
            ))

;; main

(def pages
  {:home #'home/home
   })

(defn main []
  [:<>
   [router pages]])

