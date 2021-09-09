(ns tickets.api
  (:require [ring.util.response :refer [response content-type]]
            [tickets.components.db :as db-component]
            [tickets.util.ring :as ring-util]))


(defn tickets-list
  [db]
  (let [tickets (db-component/get-ticket-list db)]
    (ring-util/json-response tickets)))


(defn tickets-create
  [db request]
  (let [ticket-data (:params request)
        ; TODO: add validation errors and catch it!
        created-ticket (db-component/create-ticket! db ticket-data)]
    (ring-util/json-response created-ticket)))
