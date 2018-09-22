(ns fulfillmint.service.etsy.api-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [clojure.string :as str]
            [fulfillmint.service.etsy.api :refer [->orders]]
            [fulfillmint.service.util :refer [->service-id]]))

(def receipt-1 {:is_gift false
                :message_from_buyer ""
                :name "Kaylee Frye"
                :needs_gift_wrap false
                :receipt_id 420022})
(def receipt-2 {:is_gift false
                :message_from_buyer "Yes I'd like a working one thanks"
                :name "Mal Reynolds"
                :needs_gift_wrap false
                :receipt_id 420021})

(def receipt-results
  [receipt-1
   receipt-2])

(def receipt-1-txn-1 {:listing_id 621190191
                      :receipt_id 420022
                      :quantity 4
                      :title "Tomatoes"
                      :variations [{:formatted_name "Options"
                                    :formatted_value "Fresh"
                                    :property_id 513
                                    :value_id 388633835397}]})
(def receipt-1-txn-2 {:listing_id 621190192
                      :receipt_id 420022
                      :quantity 3
                      :title "Strawberries"
                      :variations [{:formatted_name "Options"
                                    :formatted_value "Tasty"
                                    :property_id 513
                                    :value_id 388633835397}]})

(def receipt-2-txn-1 {:listing_id 621190190
                      :receipt_id 420021
                      :quantity 1
                      :title "Grav Boot Compression Coil",
                      :variations [{:formatted_name "Options"
                                    :formatted_value "Functional"
                                    :property_id 513
                                    :value_id 388633835397}
                                   {:formatted_name "Color"
                                    :formatted_value "Gold"
                                    :property_id 200
                                    :value_id 52041479589}]})

(def transaction-results
  [receipt-1-txn-1
   receipt-1-txn-2
   receipt-2-txn-1])

(def expected-order-1-prod-1 {:service-id (->service-id :etsy 621190191)
                              :quantity 4
                              :variants [(->service-id :etsy 388633835397)]
                              :notes (str/join
                                       "\n"
                                       ["Tomatoes"
                                        "Options: Fresh"])})
(def expected-order-1-prod-2 {:service-id (->service-id :etsy 621190192)
                              :quantity 3
                              :variants [(->service-id :etsy 388633835397)]
                              :notes (str/join
                                       "\n"
                                       ["Strawberries"
                                        "Options: Tasty"])})

(def expected-order-1
  {:service-id (->service-id :etsy 420022)
   :link "https://www.etsy.com/your/orders/sold?order_id=420022"
   :buyer-name "Kaylee Frye"
   :notes ""
   :pending? true
   :products [expected-order-1-prod-1
              expected-order-1-prod-2]})

(def expected-order-2 {:service-id (->service-id :etsy 420021)
                       :link "https://www.etsy.com/your/orders/sold?order_id=420021"
                       :buyer-name "Mal Reynolds"
                       :notes "Message: Yes I'd like a working one thanks"
                       :pending? true
                       :products [{:service-id (->service-id :etsy 621190190)
                                   :quantity 1
                                   :variants [(->service-id :etsy 388633835397)
                                              (->service-id :etsy 52041479589)]
                                   :notes (str/join
                                            "\n"
                                            ["Grav Boot Compression Coil"
                                             "Options: Functional"
                                             "Color: Gold"])}]})

(deftest ->orders-test
  (testing "Handles a single order"
    (is (= [expected-order-2]
           (->orders [receipt-2]
                     [receipt-2-txn-1])))

    (is (= [expected-order-1]
           (->orders [receipt-1]
                     [receipt-1-txn-1
                      receipt-1-txn-2]))))

  (testing "More txns than receipts"
    (is (= [expected-order-2]
           (->orders [receipt-2]
                     [receipt-2-txn-1
                      receipt-1-txn-1
                      receipt-1-txn-2]))))

  (testing "More receipts than txns"
    (is (= [expected-order-2]
           (->orders [receipt-2
                      receipt-1]
                     [receipt-2-txn-1]))))

  (testing "Handles multiple orders of different sizes"
    (is (= [expected-order-1
            expected-order-2]
           (->orders receipt-results
                     transaction-results)))

    ; flip the order: 1-item first
    (is (= [expected-order-2
            expected-order-1]
           (->orders [receipt-2
                      receipt-1]
                     [receipt-2-txn-1
                      receipt-1-txn-1
                      receipt-1-txn-2])))))

