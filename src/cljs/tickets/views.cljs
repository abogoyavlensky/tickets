(ns tickets.views
  (:require [re-frame.core :as re-frame]
            [tickets.router :as router]))

(defn header
  []
  [:h1 "Tickets"])

(defn home-page
  []
  (let [page-title (re-frame/subscribe [:page-title])]
    [:div [:h2 @page-title]
     [:a
      {:href (router/path-for :create-ticket)}
      "Create ticket"]]))

(defn create-ticket-page
  []
  (let [page-title (re-frame/subscribe [:page-title])]
    [:div [:h2 @page-title]
     [:a
      {:href (router/path-for :home)}
      "Back to list"]]))


(defn page-not-found
  []
  [:div
   [:h2 "Page not found."]])

(defn main-panel []
  (let [current-page (re-frame/subscribe [:current-page])
        content (case @current-page
                  :home home-page
                  :create-ticket create-ticket-page
                  page-not-found)]
    [:div
     [header]
     [content]]))
