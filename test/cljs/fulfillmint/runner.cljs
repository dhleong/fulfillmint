(ns fulfillmint.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [fulfillmint.core-test]))

(doo-tests 'fulfillmint.core-test)
