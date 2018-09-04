(ns fulfillmint.data.db-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [datascript.core :as d]
            [fulfillmint.data.db :as db]))

(deftest create-part-test
  (let [db (-> (d/empty-db)
               (d/db-with (db/create-part-tx
                            {:name "Compression Coil Catalyzer"
                             :unit "coils"})))]
    (testing "create-part works"
      (is (= {:db/id 1 :unit "coils"}
             (-> (db/all-parts db)
                 first
                 (select-keys [:unit :db/id])))))))

