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
                       {:db/id 2 :name "Stabilizer" :unit "things"}))))

    (testing "it handles nil quantity"
      (let [db (-> db (d/db-with (db/create-part-tx
                                   {:name "Stabilizer"
                                    :quantity nil})))
            all-parts (db/all-parts db)]
        (is (= 2 (count all-parts)))
        (is (contains? (->> all-parts
                            (map #(select-keys % [:name :quantity]))
                            (into #{}))
                       {:name "Stabilizer" :quantity 0}))))))

(deftest update-part-test
  (let [db (-> (empty-db)
               (d/db-with (db/create-part-tx
                            {:name "Compression Coil Catalyzer"
                             :unit "coils"})))
        original-part (->> db
                           db/all-parts
                           first)
        part-id (:db/id original-part)]
    (testing "Update single key"
      (is (= 0 (:quantity original-part)))

      (let [with-update (d/db-with
                          db
                          (db/upsert-part-tx
                            {:id part-id
                             :quantity 42}))
            updated (db/entity-by-id with-update part-id)]
        (is (= 42 (:quantity updated)))))))

(deftest product-test
  (let [db (-> (empty-db)
               (d/db-with (db/create-part-tx
                            {:name "Compression Coil Catalyzer"
                             :unit "coils"}))
               (d/db-with (db/upsert-product-tx
                            {:name "Firefly"
                             :service-ids ["firefly/03-k64"]
                             :variants
                             [{:name "Serenity"
                               :service-ids ["firefly/serenity"]
                               :default? true
                               :parts {1 1}}]}))
               (d/db-with (db/upsert-product-tx
                            {:name "Captain"
                             :service-ids ["firefly/captain"]
                             :variants
                             [{:name "Mal Reynolds"
                               :service-ids ["firefly/mreynolds"]
                               :default? true
                               :parts {1 1}}]})))]
    (testing "upsert-product creates"
      (let [all-products (db/all-products db)]
        (is (= 2 (count all-products)))))

    (testing "upsert-product updates"
      (let [with-update (d/db-with db (db/upsert-product-tx
                                        {:name "Firefly2"
                                         :service-ids ["firefly/03-k64"]
                                         :variants
                                         [{:id 3
                                           :name "Serenity2"
                                           :service-ids ["firefly/serenity"]
                                           :default? true
                                           :parts {1 2}}]}))
            updated (db/product-by-id with-update [:service-ids "firefly/03-k64"])]
        (is (= 2 (count (db/all-products with-update))))
        (is (= "Firefly2" (:name updated)))
        (is (= "Serenity2" (-> updated :variants first :name)))
        (is (= [{:part-use/part {:db/id 1}
                 :part-use/units 2}]
               (->> updated :variants first :variant/parts
                    (map #(select-keys % [:part-use/part :part-use/units])))
               ))))

    (testing "variants-for-product query"
      (let [variants (db/variants-for-product db [:service-ids "firefly/03-k64"])]
        (is (= ["Serenity"]
               (->> variants
                    (map :name)))))
      (let [variants (db/variants-for-product db [:service-ids "firefly/captain"])]
        (is (= ["Mal Reynolds"]
               (->> variants
                    (map :name))))))))
