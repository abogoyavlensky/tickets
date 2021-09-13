(ns tickets.front_test
  (:require [clojure.test :refer :all]
            [etaoin.api :as etaoin]
            [tickets.testing-utils :as utils]
            [tickets.queries :as queries]))


(use-fixtures :each utils/fixture-system)


(deftest test-front-create-ticket-and-get-list-ok
  (testing "check creating ticket and showing it on the list page"
    (etaoin/with-chrome {} driver
      (etaoin/go driver utils/TEST-URL-FRONT-TICKETS)
      (etaoin/wait-visible driver {:fn/has-text "There are no tickets yet."})
      (etaoin/click driver {:tag :a
                            :fn/text "Create ticket"})
      (etaoin/wait-visible driver {:fn/has-text "Save"})
      (etaoin/fill driver {:tag :input
                           :name :title} "New ticket")
      (etaoin/fill driver {:tag :textarea
                           :name :description} "Some description of a ticket.")
      (etaoin/fill driver {:tag :input
                           :name :applicant} "User Name")
      (etaoin/fill driver {:tag :input
                           :name :executor} "Staff User")
      (etaoin/fill driver {:tag :input
                           :name :completed-at} "15-09-2021")
      (etaoin/click driver {:tag :button
                            :fn/text "Save"})
      (etaoin/wait-visible driver {:fn/has-text "Ticket list"})
      (is (etaoin/visible? driver {:tag :td
                                   :fn/text "New ticket"}))))
  (testing "check ticket has been created in db"
    (let [ticket-from-db (first (queries/get-ticket-list (:db utils/*test-system*)))]
      (is (= {:applicant "User Name"
              :completed-at #inst "2021-09-15"
              :description "Some description of a ticket."
              :executor "Staff User"
              :title "New ticket"}
             (dissoc ticket-from-db :id))))))


(deftest test-front-try-create-ticket-and-get-validation-error
  (testing "check trying to create ticket and showing validation error"
    (etaoin/with-chrome {} driver
      (etaoin/go driver utils/TEST-URL-FRONT-TICKETS)
      (etaoin/wait-visible driver {:fn/has-text "There are no tickets yet."})
      (etaoin/click driver {:tag :a
                            :fn/text "Create ticket"})
      (etaoin/wait-visible driver {:fn/has-text "Save"})
      (etaoin/fill driver {:tag :textarea
                           :name :description} "Some description of a ticket.")
      (etaoin/fill driver {:tag :input
                           :name :applicant} "User Name")
      (etaoin/fill driver {:tag :input
                           :name :executor} "   ")
      (etaoin/fill driver {:tag :input
                           :name :completed-at} "159999092021")
      (etaoin/click driver {:tag :button
                            :fn/text "Save"})
      (etaoin/wait-visible driver {:fn/has-text "Title is required"})
      (is (etaoin/visible? driver {:tag :p
                                   :class :form-input-hint
                                   :fn/text "Title is required."}))
      (is (etaoin/visible? driver {:tag :p
                                   :class :form-input-hint
                                   :fn/text "Executor value is empty string."}))
      (is (etaoin/visible? driver {:tag :p
                                   :class :form-input-hint
                                   :fn/text "Completion date value has invalid format."})))))


(deftest test-front-page-not-found-ok
  (testing "check response is empty if there is no tickets"
    (etaoin/with-chrome {} driver
      (etaoin/go driver (str utils/TEST-URL-BASE "/wrong"))
      (etaoin/wait-visible driver {:fn/has-text "Tickets"})
      (is (etaoin/visible? driver {:tag :h2
                                   :fn/text "Page not found."})))))
