(ns fulfillmint.runner
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]
            [fulfillmint.data.db-test]))

(doo-all-tests #"fulfillmint\..*-test")
