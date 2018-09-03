(ns fulfillmint.events
  (:require [re-frame.core :refer [dispatch reg-event-db reg-event-fx
                                   path
                                   inject-cofx trim-v]]
            [fulfillmint.db :as db]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            ))

(reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(reg-event-db
  :navigate!
  [trim-v]
  (fn-traced [db page-spec]
    (assoc db :page page-spec)))

