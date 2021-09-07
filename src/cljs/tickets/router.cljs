(ns tickets.router
  (:require [re-frame.core :as re-frame]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]))


(def routes
  ["/testapp" {"" :home
               "/create-ticket" :create-ticket}])


; Duplicates api routes cause compojure does not support cljs.
; Note: We could use bidi or reitit to get backend routes directly.
(def api-routes
  ["/api/"
   [["tickets" :api-tickets-list]]])


(def history
  (pushy/pushy
    #(re-frame/dispatch [:set-current-page (:handler %)])
    #(bidi/match-route routes %)))


(defn path-for
  [page]
  (bidi/path-for routes page))


(defn path-for-api
  [route]
  (bidi/path-for api-routes route))
