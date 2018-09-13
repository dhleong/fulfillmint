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
