(ns fulfillmint.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

; ======= Core ============================================

(reg-sub :page :page)

(reg-sub
  :new?
  :<- [:parts]
  :<- [:products]
  (fn [things _]
    (not (every? seq things))))
