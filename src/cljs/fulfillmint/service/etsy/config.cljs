(ns ^{:author "Daniel Leong"
      :doc "service.etsy.config"}
  fulfillmint.service.etsy.config
  (:require-macros [fulfillmint.util.log :refer [log]])
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [alandipert.storage-atom :refer [local-storage]]
            [fulfillmint.util :refer [fn-click]]))

(def ^:private login-url (if goog.DEBUG
                           "http://localhost:3000/oauth"
                           "http://fulfillmint.now.sh/oauth"))

(def ^:private login-window-opts (->> {:resizable 1
                                       :scrollbars 1
                                       :width 500
                                       :height 550}
                                      (map (fn [[k v]]
                                             (str (name k) "=" v)))
                                      (str/join ",")))

(defonce oauth-storage (local-storage (r/atom nil) :etsy-oauth))

(defn ^:export on-login-result [search-str]
  (log "on-login-result" search-str)
  (let [params (-> search-str
                   (subs 1)
                   (str/split #"&")
                   (->> (map (fn [var-pair]
                               (let [[k v] (str/split var-pair #"=")]
                                 [(case k
                                    "oauth_token" :token
                                    "oauth_token_secret" :secret)
                                  v])))
                        (into {})))]
    (reset! oauth-storage params)
    (log "login-result" params)))

(defn- open-login-window []
  (doto (js/window.open
          login-url
          "_blank"
          login-window-opts)
    (.focus)))

(defn view []
  [:<>
   [:h4 "Configure Etsy"]

   (if @oauth-storage
     [:<>
      [:div "Logged In"]
      [:input
       {:type 'button
        :on-click (fn-click
                    (log "logout")
                    (reset! oauth-storage nil))
        :value "Log out"}]]

     [:<>
      [:input
       {:type 'button
        :on-click (fn-click
                    (log "login")
                    (open-login-window))
        :value "Login"}]])])
