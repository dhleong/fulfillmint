(ns ^{:author "Daniel Leong"
      :doc "service.util"}
  fulfillmint.service.util)

(defn ->service-id
  [service-key id-str]
  (str (name service-key) "~" id-str))
