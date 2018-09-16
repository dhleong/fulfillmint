(ns fulfillmint.runner
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [fulfillmint.data.db-test]
            [fulfillmint.service.util-test]
            [fulfillmint.views.new-product-test]))

(doo-all-tests #"fulfillmint\..*-test")
