(ns tickets.routes
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [compojure.core :refer [ANY GET PUT POST DELETE routes context]]
            [compojure.route :as route]
            [ring.util.response :refer [response content-type]]
            [tickets.components.db :as db-component]))

(defn json-response
  "Return data as ring response in json format."
  [data]
  (-> (response (json/generate-string data))
      (content-type "application/json")))


(defn tickets-list
  [db]
  (let [tickets (db-component/get-ticket-list db)]
    (json-response tickets)))


(defn tickets-create
  [_])


(defn api-routes
  [endpoint]
  (routes
    (GET "/tickets" [] (tickets-list (:db endpoint)))))


(defn home-routes
  [endpoint]
  (routes
    (context "/api" []
      (api-routes endpoint))
    (GET "/" _
      (-> "public/index.html"
          io/resource
          io/input-stream
          response
          (assoc :headers {"Content-Type" "text/html; charset=utf-8"})))
    (route/resources "/")
    (route/not-found "Page not found.")))
