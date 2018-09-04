(ns ^{:author "Daniel Leong"
      :doc "Interaction with re-frame DB"}
  fulfillmint.data.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [fulfillmint.data.db :as db]))

(reg-sub
  :parts
  :<- [::db]
  (fn [db _]
    (println "running :parts")
    (->> (db/all-parts db)
         (map (fn [p]
                (-> p
                    (dissoc :db/id)
                    (assoc :id (:db/id p))))))))
