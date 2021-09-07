(ns tickets.routes
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET POST routes context]]
            [compojure.route :as route]
            [ring.util.response :refer [response content-type]]
            [tickets.components.db :as db-component]
            [tickets.util.ring :as ring-util]))


(defn tickets-list
  [db]
  (let [tickets (db-component/get-ticket-list db)]
    (ring-util/json-response tickets)))


(defn tickets-create
  [db request]
  (let [ticket-data (:params request)]
    ; TODO: add validation errors and catch it!
    (db-component/create-ticket! db ticket-data)
    (ring-util/json-response ticket-data)))


(defn api-routes
  [endpoint]
  (context "/tickets" []
    (routes
      (GET "/" [] (tickets-list (:db endpoint)))
      (POST "/" request (tickets-create (:db endpoint) request)))))


(defn home-routes
  [endpoint]
  (routes
    (context "/api" []
      (api-routes endpoint))
    (route/resources "/assets")
    (GET "/*" _
      (-> "public/index.html"
          io/resource
          io/input-stream
          response
          (content-type "text/html; charset=utf-8")))
    (route/not-found "Page not found.")))
