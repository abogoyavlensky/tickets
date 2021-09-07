(ns tickets.router
  (:require [re-frame.core :as re-frame]
            [pushy.core :as pushy]
            [bidi.bidi :as bidi]))


(def routes
  ["/testapp" {"" :home
               "/create-ticket" :create-ticket}])


(def history
  (pushy/pushy
    #(re-frame/dispatch [:set-current-page (:handler %)])
    #(bidi/match-route routes %)))


(defn path-for
  [page]
  (bidi/path-for routes page))
