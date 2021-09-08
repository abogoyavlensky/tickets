(ns tickets.views
  (:require [re-frame.core :as re-frame]
            [tickets.router :as router]))

(defn- header
  []
  [:h1 "Tickets"])


(defn- render-ticket-item
  [ticket]
  [:tr
   {:key (:id ticket)}
   [:td (:title ticket)]
   [:td (:description ticket)]
   [:td (:applicant ticket)]
   [:td (:executor ticket)]
   [:td (:completed-at ticket)]])


(defn- render-tickets-table
  [tickets]
  (if (seq tickets)
    [:table
     [:thead
      [:tr
       [:th "Title"]
       [:th "Description"]
       [:th "Applicant"]
       [:th "Executor"]
       [:th "Completion date"]]]
     [:tbody
      (map render-ticket-item tickets)]]
    [:p "There are no tickets yet. Please create a new ticket."]))


(defn- home-page
  []
  (let [page-title (re-frame/subscribe [:page-title])
        tickets (re-frame/subscribe [:tickets])
        error (re-frame/subscribe [:tickets-error])
        loading? (re-frame/subscribe [:tickets-loading?])]
    [:div [:h2 @page-title]
     [:a
      {:href (router/path-for :create-ticket)}
      "Create ticket"]
     [:div
      (if (true? @loading?)
         [:p "Loading..."]
         (if (some? @error)
           [:p @error]
           (render-tickets-table @tickets)))]]))


(defn- create-ticket-page
  []
  (let [page-title (re-frame/subscribe [:page-title])]
    [:div [:h2 @page-title]
     [:a
      {:href (router/path-for :home)}
      "Back to list"]]))


(defn- page-not-found
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
