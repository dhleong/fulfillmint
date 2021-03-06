(ns fulfillmint.subs
  (:require [clojure.string :as str]
            [re-frame.core :refer [reg-sub subscribe]]))

; ======= Core ============================================

(reg-sub :page :page)

(reg-sub
  :new?
  :<- [:parts]
  :<- [:products]
  (fn [things _]
    (not (every? seq things))))


; ======= searchable ======================================

(reg-sub :searches :searches)

(reg-sub
  :search
  (fn [[_ query-sub]]
    [(subscribe query-sub)
     (subscribe [:searches])])
  (fn [[inputs searches] [_ query-sub opts]]
    (let [{:keys [->searchable]
           :or {->searchable :name}} opts
          query (get searches query-sub)]
      (cond->> inputs
        (not (str/blank? query))
        (filter (fn [r]
                  (str/includes?
                    (str/lower-case (->searchable r))
                    (str/lower-case query))))))))


; ======= computed ========================================

(reg-sub
  :parts-by-id
  :<- [:parts]
  (fn [parts]
    (reduce
      (fn [m p]
        (assoc m (:id p) p))
      {}
      parts)))


; ======= reports =========================================

(reg-sub
  :parts-needed-for-orders
  :<- [:parts-for-orders]
  :<- [:parts-by-id]
  (fn [[for-orders by-id]]
    (->> for-orders
         (map (fn [for-order]
                (assoc for-order
                       :available
                       (-> by-id
                           (get (:id for-order))
                           :quantity))))
         (sort-by (comp :name :part)))))

(reg-sub
  :ordered-variants-for-part
  :<- [:variants-for-orders]
  (fn [variants-ordered [_ part-id]]
    (let [part-id (int part-id)] ; FIXME yuck?
      (->> variants-ordered
           (filter (fn [{:keys [parts]}]
                     (contains? parts part-id)))
           (reduce
             (fn [m {:keys [variant ordered]}]
               (assoc m (:id variant) ordered))
             {})))))

(reg-sub
  :ordered-variants
  :<- [:variants-for-orders]
  (fn [variants-ordered _]
    (->> variants-ordered

         ; group products together
         (sort-by (juxt (comp :name :product)
                        :name)))))
