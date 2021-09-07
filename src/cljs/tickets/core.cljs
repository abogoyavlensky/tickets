(ns tickets.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [pushy.core :as pushy]
            [tickets.events]
            [tickets.subs]
            [tickets.views :as views]
            [tickets.config :as config]
            [tickets.router :as router]))


(enable-console-print!)


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))


(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn render []
  (pushy/start! router/history)
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
