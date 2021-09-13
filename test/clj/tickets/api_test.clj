(ns tickets.api-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [tickets.testing-utils :as utils]
            [tickets.queries :as queries]
            [tickets.handlers :as handlers]))


(use-fixtures :each utils/fixture-system)


(deftest test-api-tickets-list-empty
  (testing "check response is empty if there is no tickets"
    (let [response (client/get utils/TEST-URL-API-TICKETS
                               {:accept :json
                                :as :json})]
      (is (= 200 (:status response)))
      (is (= [] (:body response))))))


(deftest test-api-tickets-returning-items-ok
  (testing "check tickets in response"
    (queries/create-ticket!
      (:db utils/*test-system*)
      {:title "First ticket"
       :description "Second description for the ticket."
       :applicant "User Name 1"
       :executor "User From-Staff 1"
       :completed-at #inst "2021-09-22"})
    (queries/create-ticket!
      (:db utils/*test-system*)
      {:title "Second ticket"
       :description "Second description for the ticket."
       :applicant "User Name 2"
       :executor "User From-Staff 2"
       :completed-at #inst "2021-11-03"})
    (let [response (client/get utils/TEST-URL-API-TICKETS
                               {:accept :json
                                :as :json})]
      (is (= 200 (:status response)))
      (is (= #{{:title "First ticket"
                :description "Second description for the ticket."
                :applicant "User Name 1"
                :executor "User From-Staff 1"
                :completed-at "2021-09-22"}
               {:title "Second ticket"
                :description "Second description for the ticket."
                :applicant "User Name 2"
                :executor "User From-Staff 2"
                :completed-at "2021-11-03"}}
             (->> (:body response)
                  (map #(dissoc % :id))
                  (set)))))))


(deftest test-api-create-ticket-ok
  (testing "check that there are no tickets in db"
    (is (nil? (queries/get-ticket-list (:db utils/*test-system*) {}))))
  (let [params {:title "New ticket"
                :description "Some description for the ticket."
                :applicant "User Name"
                :executor "User From-Staff"
                :completed-at "2021-09-22"}
        response (client/post utils/TEST-URL-API-TICKETS
                              {:accept :json
                               :content-type :json
                               :as :json
                               :body (json/generate-string params)})]
    (testing "check that response fro creating ticket is correct"
      (is (= 200 (:status response)))
      (is (= params (dissoc (:body response) :id)))
      (is (integer? (get-in response [:body :id]))))
    (testing "check ticket has been created in db"
      (let [ticket-from-db (first (queries/get-ticket-list (:db utils/*test-system*) {}))]
        (is (= (update ticket-from-db :completed-at #'handlers/date->string)
               (:body response)))))))


(deftest test-api-create-ticket-with-validation-err
  (let [params {:title "   "
                :applicant "User Name"
                :executor "User From-Staff"
                :completed-at "wrong"}
        response (client/post utils/TEST-URL-API-TICKETS
                              {:accept :json
                               :content-type :json
                               :as :json
                               :throw-exceptions false
                               :body (json/generate-string params)})]
    (is (= 400 (:status response)))
    (is (= {"error-code" ":params/validation"
            "errors" {"completed-at" ["Completion date value has invalid format."]
                      "description" ["Description is required."]
                      "title" ["Title value is empty string."]}
            "status" "error"}
           (json/decode (:body response))))))
