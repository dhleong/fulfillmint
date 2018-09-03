(ns ^{:author "Daniel Leong"
      :doc "Navigation util"}
  fulfillmint.util.nav
  (:require [clojure.string :as string]
            [reagent.dom :as reagent-dom]
            [re-frame.core :refer [dispatch-sync]]
            [secretary.core :as secretary]
            [goog.events :as gevents]
            [goog.history.EventType :as HistoryEventType]
            [pushy.core :as pushy]
            [fulfillmint.util :refer [is-ios? >evt]])
  (:import goog.History))

(goog-define ^boolean LOCAL false)

; NOTE: figwheel css live-reload doesn't work so well with
; the fancy nav
(def ^:private pushy-supported? (and (not LOCAL)
                                     (pushy/supported?)))

(def ^:private pushy-prefix "/fulfillmint")

(defn init! []
  (secretary/set-config! :prefix (if pushy-supported?
                                   (str pushy-prefix "/")
                                   "#")))

;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (if pushy-supported?
    ; fancy html5 navigation
    (let [history (pushy/pushy
                    secretary/dispatch!
                    (fn [x]
                      (let [[uri-path query-string]
                            (string/split (secretary/uri-without-prefix x) #"\?")
                            uri-path (secretary/uri-with-leading-slash uri-path)]
                        (when (secretary/locate-route uri-path)
                          x))))]
      (pushy/start! history))

    ; #-based navigation
    (doto (History.)
      (gevents/listen
        HistoryEventType/NAVIGATE
        (fn [event]
          (secretary/dispatch! (.-token event))))
      (.setEnabled true))))

(defn prefix
  "Prefix a link as necessary for :href-based navigation to work"
  [raw-link]
  (if pushy-supported?
    (str pushy-prefix raw-link)
    (str "#" raw-link)))

(defn navigate!
  [& args]
  (let [evt (into [:navigate!] args)]
    (if (is-ios?)
      ; NOTE: on iOS we do some whacky shirt to prevent awful flashes
      ;  when swiping back. hopefully there's a more efficient way
      ;  to do this, but for now... this works
      (do
        (dispatch-sync evt)
        (reagent-dom/force-update-all))

      ; When we don't have to worry about back-swipe, we can be more
      ;  relaxed about handling navigation
      (>evt evt))))

(defn replace!
  "Wrapper around js/window.location.replace"
  [new-location]
  (js/window.location.replace
    (prefix new-location)))
