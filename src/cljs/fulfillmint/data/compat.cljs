(ns ^{:author "Daniel Leong"
      :doc "compat"}
  fulfillmint.data.compat
  (:require [re-frame.core :refer [dispatch reg-event-db reg-event-fx
                                   reg-sub
                                   path
                                   inject-cofx trim-v]]
            [fulfillmint.data.db :refer [conn]]))

(def initial-db-state {::ts 0})

(defn reg-event-conn
  "Register an event handler that gets the datascript
   connection as its first argument"
  ([n handler-fn]
   (reg-event-conn n nil handler-fn))
  ([n interceptors handler-fn]
   (reg-event-db
     n
     interceptors
     (fn [db args]
       ; perform the operation and ignore the results
       (handler-fn conn args)

       ; update the DB to let subscriptions know data/db has changed
       (assoc db ::ts (js/Date.now))))))


; base subscriptions that provide the current db state
(reg-sub ::ts ::ts)
(reg-sub
  :fulfillmint.data.subs/db
  :<- [::ts]
  (fn [_ _]
    @conn))
