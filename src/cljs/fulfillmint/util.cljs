(ns ^{:author "Daniel Leong"
      :doc "util"}
  fulfillmint.util
  (:require-macros [fulfillmint.util :refer [fn-click]])
  (:require [re-frame.core :refer [subscribe dispatch]]))

(def <sub (comp deref subscribe))
(def >evt dispatch)

(def is-ios?
  (memoize
    (fn is-ios? []
      (and (boolean js/navigator.platform)
           (re-find #"iPad|iPhone|iPod" js/navigator.platform)))))

(defn click>evts
  "Returns an on-click handler that dispatches the given events
   and prevents the default on-click events"
  [& events]
  (fn-click [e]
    (doseq [event events]
      (>evt event))

    ; always prevent propagation
    (.stopPropagation e)))

(defn click>evt
  "Returns an on-click handler that dispatches the given event
   and prevents the default on-click events"
  [event & {:keys [propagate?]
            :or {propagate? true}}]
  (fn-click [e]
    (>evt event)

    ; prevent propagation, optionally
    (when-not propagate?
      (.stopPropagation e))))

(defn click>reset!
  "Returns an on-click handler that performs (reset!) with
   the given arguments"
  [a v]
  (fn-click
    (reset! a v)))

(defn click>swap!
  "Returns an on-click handler that performs (swap!) with the
   given arguments"
  ([a f]
   (fn-click
     (swap! a f)))
  ([a f x]
   (fn-click
     (swap! a f x)))
  ([a f x y]
   (fn-click
     (swap! a f x y))))

