(ns ^{:author "Daniel Leong"
      :doc "api"}
  fulfillmint.service.etsy.api
  (:require [clojure.core.async :refer [promise-chan put!]]
            [ajax.core :refer [POST]]
            [fulfillmint.service.etsy.creds :refer [oauth-storage]]
            ))

(def proxy-root (if goog.DEBUG
                  "http://localhost:3000"
                  "http://fulfillmint.now.sh"))

(def ^:private proxy-url (str proxy-root "/proxy"))

(defn- post-api [method params]
  (when-let [{:keys [token secret]} @oauth-storage]
    (let [ch (promise-chan)]
      (POST proxy-url
            {:format :json
             :params (merge
                       {:oauth-token token
                        :oauth-secret secret
                        :method method}
                       params)
             :handler (fn [result]
                        (put! ch [nil result]))
             :error-handler (fn [e]
                              (put! ch [e]))})
      ch)))

(defn get-self []
  (post-api :GET {:url "/users/__SELF__/profile"}))
