(ns tickets.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [pushy.core :as pushy]
            [tickets.events]
            [tickets.subs]
            [tickets.views :as views]
            [tickets.router :as router]))


(enable-console-print!)


(def debug?
  ^boolean goog.DEBUG)


(defn dev-setup
  []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))


(defn mount-root
  []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn render
  []
  (pushy/start! router/history)
  (re-frame/dispatch-sync [:event/initialize-db])
  (dev-setup)
  (mount-root))
