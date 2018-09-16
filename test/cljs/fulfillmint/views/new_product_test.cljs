(ns fulfillmint.views.new-product-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [fulfillmint.service.util :refer [->service-id]]
            [fulfillmint.views.new-product :as np
             :refer [clean-product product->form]]))

(def parts-by-id
  {1 {:kind :part
      :name "Gold Beads"
      :quantity 0
      :supplier ""
      :unit "beads"
      :id 1}})

(def from-form
  {:name "Product Name"
   ::np/services [{:service :etsy, :id 123}]
   :variants [{::np/services [{:service :etsy, :id 321}]
               :name "Var 1"
               :group "group"
               ::np/parts {0 {:part (get parts-by-id 1)
                              :quantity 12}}}]})

(def cleaned-product
  {:name "Product Name"
   :service-ids [(->service-id :etsy 123)]
   :variants [{:service-ids [(->service-id :etsy 321)]
               :name "Var 1"
               :group "group"
               :parts {1 12}}]})

(deftest clean-product-test
  (testing "Clean product"
    (is (= cleaned-product
           (clean-product from-form)))))

(def from-db
  {:id 11
   :kind :product
   :name "Product Name"
   :service-ids ["etsy~123"]
   :variants [{:id 12
               :kind :variant
               :name "Var 1"
               :service-ids ["etsy~321"]
               :variant/group "group"
               :variant/parts [{:id 14
                                :kind :part-use
                                :part-use/part {:db/id 1}
                                :part-use/units 12}]
               :variant/product {:db/id 11}}]})

(deftest product->form-test
  (testing "Product -> form"
    (is (= (-> from-form
               (assoc :id 11)
               (update-in [:variants 0] assoc :id 12))
           (product->form
             parts-by-id
             from-db)))))
