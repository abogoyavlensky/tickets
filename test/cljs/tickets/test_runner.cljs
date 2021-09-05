(ns tickets.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [tickets.core-test]
   [tickets.common-test]))

(enable-console-print!)

(doo-tests 'tickets.core-test
           'tickets.common-test)
