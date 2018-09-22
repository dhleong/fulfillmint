(ns ^{:author "Daniel Leong"
      :doc "api"}
  fulfillmint.service.etsy.api
  (:require-macros [fulfillmint.util.log :as log])
  (:require [clojure.core.async :as async :refer [go promise-chan put!]]
            [clojure.string :as str]
            [ajax.core :refer [POST]]
            [cemerick.url :refer [map->query]]
            [fulfillmint.service.etsy.creds :refer [oauth-storage]]
            [fulfillmint.service.util :refer [->service-id]]
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
  (let [params (merge {:limit 50
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


; ======= API composition =================================

(defn- receipt-id->link
  "Given a *receipt ID*, generate a link to view it on Etsy.
   Note that while the URL says 'order id,' it is, in fact,
   the receipt-id it wants"
  [id]
  (str "https://www.etsy.com/your/orders/sold?order_id=" id))

(defn- ->order [receipt]
  {:service-id (->service-id :etsy (:receipt_id receipt))
   :link (receipt-id->link (:receipt_id receipt))
   :buyer-name (:name receipt)
   :pending? true
   :notes (or (when-not (str/blank? (:message_from_buyer receipt))
                (str "Message: " (:message_from_buyer receipt)))
            "")
   :products []})

(defn- ->product [txn]
  {:service-id (->service-id :etsy (:listing_id txn))
   :quantity (:quantity txn)
   :variants (->> txn
                  :variations
                  (map #(->service-id :etsy (:value_id %))))
   :notes (str/join
            "\n"
            (concat
              [(:title txn)]
              (->> txn
                   :variations
                   (map #(str (:formatted_name %) ": "
                              (:formatted_value %))))))})

(defn ->orders
  "Given a seq of receipts (which include messages from the buyer)
   and of transactions (which include the actual items ordered)
   return a coll of *orders*, which have both"
  [receipts transactions]
  (loop [receipts receipts
         transactions transactions
         orders []
         last-order nil]
    (let [current-receipt (first receipts)
          current-transaction (first transactions)
          current-order (or last-order
                            (when current-receipt
                              (->order current-receipt)))]
      (cond
        ; no more receipts or transactions? return what we've got
        (or (nil? current-receipt)
            (nil? current-transaction))
        orders

        ; transaction belongs to this receipt;
        ; add it to the current-order and look at the next transaction
        (= (:receipt_id current-receipt)
           (:receipt_id current-transaction))
        (let [with-product (update current-order
                                   :products
                                   conj
                                   (->product current-transaction))]
          (recur
            receipts
            (next transactions)
            (conj (if (identical? last-order current-order)
                    ; remove the old version so we can conj the new one
                    ; with the new product added
                    (pop orders)
                    orders)
                  with-product)
            with-product))

        ; otherwise, transaction belongs to a new receipt
        :else
        (recur
          (next receipts)
          transactions
          orders
          nil) ; new order
        ))))

(defn get-orders []
  (->> [(get-receipts)
        (get-transactions)]
       (async/map
         (fn [[e1 receipts] [e2 transactions]]
           (if-let [e (or e1 e2)]
             (do
               (log/warn "Error fetching orders:" e)
               [e nil])
             [nil (->orders (:results receipts)
                            (:results transactions))])))))
