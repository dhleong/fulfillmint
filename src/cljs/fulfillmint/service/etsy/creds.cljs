(ns ^{:author "Daniel Leong"
      :doc "creds"}
  fulfillmint.service.etsy.creds
  (:require [reagent.core :as r]
            [alandipert.storage-atom :refer [local-storage]]))

(defonce oauth-storage (local-storage (r/atom nil) :etsy-oauth))
