(ns fulfillmint.service.util-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [fulfillmint.service.util :refer [->service-id
                                              unpack-service-id]]))

(deftest service-id-test
  (testing "Pack service-id"
    (is (= "etsy~123"
           (->service-id :etsy 123))))

  (testing "Unpack service-id"
    (is (= [:etsy "123"]
           (unpack-service-id "etsy~123")))))

