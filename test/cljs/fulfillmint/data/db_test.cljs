(ns fulfillmint.data.db-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [datascript.core :as d]
            [fulfillmint.data.db :as db]
            [fulfillmint.data.schema :refer [schema]]))

(defn empty-db []
  (d/empty-db schema))

(deftest create-part-test
  (let [db (-> (empty-db)
               (d/db-with (db/create-part-tx
                            {:name "Compression Coil Catalyzer"
                             :unit "coils"})))]

    (testing "create-part works"
      (let [all-parts (db/all-parts db)]
        (is (= 1 (count all-parts)))
        (is (= {:db/id 1 :unit "coils"}
               (-> all-parts
                   first
                   (select-keys [:unit :db/id]))))))

    (testing "create-part works a second time"
      (let [db (-> db (d/db-with (db/create-part-tx
                                   {:name "Stabilizer"})))
            all-parts (db/all-parts db)]
        (is (= 2 (count all-parts)))
        (is (contains? (->> all-parts
                            (map #(select-keys % [:name :unit :db/id]))
                            (into #{}))
                       {:db/id 2 :name "Stabilizer" :unit "things"}))))))

(deftest create-product-test
  (let [db (-> (empty-db)
               (d/db-with (db/create-part-tx
                            {:name "Compression Coil Catalyzer"
                             :unit "coils"}))
               (d/db-with (db/create-product-tx
                            {:name "Firefly"
                             :service-ids ["firefly/03-k64"]
                             :variants
                             [{:name "Serenity"
                               :service-ids ["firefly/serenity"]
                               :default? true
                               :parts {1 1}}]})))]
    (testing "create-product works"
      (let [all-products (db/all-products db)]
        (is (= 1 (count all-products)))))))
