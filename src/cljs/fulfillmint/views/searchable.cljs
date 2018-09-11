(ns ^{:author "Daniel Leong"
      :doc "searchable"}
  fulfillmint.views.searchable
  (:require-macros [fulfillmint.util.log :as log])
  (:require [clojure.string :as str]
            [reagent-forms.core :refer [bind-fields]]
            [fulfillmint.util :refer [<sub >evt]]
            [fulfillmint.views.widgets :refer [link]]))

(defn searchable [& {:keys [sub ->url]}]
  [:div.searchable
   [bind-fields
    [:div.search
     [:input {:field :text
              :id :search
              :placeholder "Search..."}]]
    {:get #(get (<sub [:searches]) sub)
     :save! #(>evt [:put-search sub %2])}]

   [:div.results
    (if-let [results (seq (<sub [:search sub]))]
      (for [r results]
        ^{:key (:id r)}
        [:div.result
         (when-not (:id r)
           (log/warn "No :id for " r))

         [link {:href (->url r)}
          (:name r)]])

      (when-not (str/blank? (get (<sub [:searches]) sub))
        [:div.no-results
         "No results"]))]])
