(ns ^{:author "Daniel Leong"
      :doc "Shared widgets"}
  fulfillmint.views.widgets
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [fulfillmint.util :refer [<sub >evt click>evt click>swap!]]
            [fulfillmint.util.nav :as nav]))


(defn link
  "Drop-in replacement for :a that inserts the # in links if necessary"
  [attrs & contents]
  (into [:a (update attrs :href nav/prefix)]
        contents))

