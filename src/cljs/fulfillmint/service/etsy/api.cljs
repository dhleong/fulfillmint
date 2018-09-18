(ns ^{:author "Daniel Leong"
      :doc "api"}
  fulfillmint.service.etsy.api
  (:require [clojure.core.async :refer [go promise-chan put!]]
            [ajax.core :refer [POST]]
            [cemerick.url :refer [map->query]]
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
             :response-format :json
             :keywords? true
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

(defn- get-paginable [url {:keys [limit offset]
                           :as params}]
  (let [params (merge {:limit 100
                       :offset 0}
                      params)
        url (str url "?" (map->query params))]
    (post-api :GET {:url url})))

(defn paginable-getter [url]
  (fn [& {:as opts}]
    (get-paginable url opts)))


; ======= API methods =====================================

(defn get-self []
  (post-api :GET {:url "/users/__SELF__/profile"}))

(def get-receipts
  (paginable-getter "/shops/__SELF__/receipts"))

(def get-transactions
  (paginable-getter "/shops/__SELF__/transactions"))
