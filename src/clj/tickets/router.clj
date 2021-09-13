(ns tickets.router
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET POST routes context]]
            [compojure.route :as route]
            [ring.util.response :refer [response content-type]]
            [tickets.handlers :as handlers]))


(defn api-routes
  [endpoint]
  (context "/tickets" []
    (routes
      (GET "/" request (handlers/tickets-list (:db endpoint) request))
      (POST "/" request (handlers/tickets-create (:db endpoint) request)))))


(defn home-routes
  [endpoint]
  (routes
    (context "/api" []
      (api-routes endpoint))
    (route/resources "/assets")
    (GET "/favicon.ico" _
      (-> "public/favicon.ico"
          io/resource
          io/input-stream
          response))
    (GET "/*" _
      (-> "public/index.html"
          io/resource
          io/input-stream
          response
          (content-type "text/html; charset=utf-8")))
    (route/not-found "Page not found.")))
