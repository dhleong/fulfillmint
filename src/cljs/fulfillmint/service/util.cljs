(ns ^{:author "Daniel Leong"
      :doc "service.util"}
  fulfillmint.service.util)

(defn ->service-id
  [service-key id-str]
  (str (name service-key) "~" id-str))

(defn unpack-service-id
  [service-id]
  (let [separator-idx (.indexOf service-id "~")]
    [(keyword (subs service-id 0 separator-idx))
     (subs service-id (inc separator-idx))]))
