(ns tickets.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
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

(defn- input-field
  [{:keys [params field label field-type submitting?]}]
  (let [field-name-str (name field)]
    [:div
     [:label {:for field-name-str} label]
     [:input
      {:id field-name-str
       :name field-name-str
       :type field-type
       :value (get @params field)
       :on-change #(swap! params assoc field (-> % .-target .-value))
       :disabled (true? submitting?)}]]))

(defn- textarea-field
  [{:keys [params field label submitting?]}]
  (let [field-name-str (name field)]
    [:div
     [:label {:for field-name-str} label]
     [:textarea
      {:id field-name-str
       :name field-name-str
       :value (get @params field)
       :on-change #(swap! params assoc field (-> % .-target .-value))
       :disabled (true? submitting?)}]]))

(defn- ticket-form
  []
  (let [ticket-form-submitting? (re-frame/subscribe [:ticket-form-submitting?])
        params (reagent/atom {:title ""})]
    (fn []
      [:form
       [input-field {:params params
                     :field :title
                     :label "Title"
                     :field-type "text"
                     :submitting? @ticket-form-submitting?}]
       [textarea-field {:params params
                        :field :description
                        :label "Description"
                        :submitting? @ticket-form-submitting?}]
       [input-field {:params params
                     :field :applicant
                     :label "Applicant"
                     :field-type "text"
                     :submitting? @ticket-form-submitting?}]
       [input-field {:params params
                     :field :executor
                     :label "Executor"
                     :field-type "text"
                     :submitting? @ticket-form-submitting?}]
       [input-field {:params params
                     :field :completed-at
                     :label "Completion date"
                     :field-type "date"
                     :submitting? @ticket-form-submitting?}]
       [:button
        {:type :button
         :disabled (true? @ticket-form-submitting?)
         :on-click #(re-frame/dispatch [:event/create-ticket])}
        "Save"]])))


(defn- create-ticket-page
  []
  (let [page-title (re-frame/subscribe [:page-title])]
    [:div [:h2 @page-title]
     [:a
      {:href (router/path-for :home)}
      "Back to list"]
     [ticket-form]]))


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
