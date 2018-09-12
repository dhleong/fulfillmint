(ns ^{:author "Daniel Leong"
      :doc "widgets.typeahead"}
  fulfillmint.views.widgets.typeahead
  (:require [clojure.string :as str]
            [fulfillmint.util :refer [<sub >evt click>reset! fn-click]]
            ))

(defn- string-or-name [v]
  (if (string? v)
    v
    (:name v)))

(defn- typeahead [& {:keys [id query-sub placeholder]}]
  [:div.form-part
   {:field :typeahead
    :id id
    :clear-on-focus? false
    :input-placeholder placeholder
    :data-source (fn [input]
                   (when-not (str/blank? input)
                     (let [match (str/lower-case input)]
                       (->> (<sub query-sub)
                            (filter
                              #(str/includes? (str/lower-case
                                                (:name %))
                                              match))))))

    ; raw value OR stored value -> display value
    :in-fn string-or-name

    ; data-source entry -> displayed value
    :result-fn :name
    }])
