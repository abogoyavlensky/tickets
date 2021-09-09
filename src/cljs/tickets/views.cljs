(ns tickets.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [tickets.router :as router]))

(defn- header
  []
  [:h1 "Tickets"])


(defn- render-ticket-item
  [active-ticket-id ticket]
  (let [tr-class (cond-> []
                   (and
                     (some? active-ticket-id)
                     (= active-ticket-id (:id ticket))) (conj "active"))]
    [:tr
     {:key (:id ticket)
      :class tr-class}
     [:td (:title ticket)]
     [:td (:description ticket)]
     [:td (:applicant ticket)]
     [:td (:executor ticket)]
     [:td (:completed-at ticket)]]))


(defn- render-tickets-table
  [tickets]
  (let [ticket-new-id (re-frame/subscribe [:ticket-new-id])]
    (if (seq tickets)
      [:table
       {:class ["table"]}
       [:thead
        [:tr
         [:th "Title"]
         [:th "Description"]
         [:th "Applicant"]
         [:th "Executor"]
         [:th "Completion date"]]]
       [:tbody
        (map (partial render-ticket-item @ticket-new-id) tickets)]]
      [:p "There are no tickets yet. Please create a new ticket."])))


(defn- home-page
  []
  (let [page-title (re-frame/subscribe [:page-title])
        tickets (re-frame/subscribe [:tickets])
        error (re-frame/subscribe [:tickets-error])
        loading? (re-frame/subscribe [:tickets-loading?])]
    [:div
     {:class ["container"]}
     [:div
      {:class ["columns"]}
      [:h2
       {:class ["col-2" "col-mr-auto"]}
       @page-title]
      [:a
       {:href (router/path-for :create-ticket)
        :class ["btn" "btn-primary" "col-2"]}
       "Create ticket"]]
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
     [:label
      {:for field-name-str
       :class ["form-label"]}
      label]
     [:input
      {:id field-name-str
       :name field-name-str
       :type field-type
       :value (get @params field)
       :on-change #(swap! params assoc field (-> % .-target .-value))
       :disabled (true? submitting?)
       :class ["form-input"]}]]))

(defn- textarea-field
  [{:keys [params field label submitting?]}]
  (let [field-name-str (name field)]
    [:div
     [:label
      {:for field-name-str
       :class ["form-label"]}
      label]
     [:textarea
      {:id field-name-str
       :name field-name-str
       :value (get @params field)
       :on-change #(swap! params assoc field (-> % .-target .-value))
       :disabled (true? submitting?)
       :class ["form-input"]}]]))

(defn- ticket-form
  []
  (let [ticket-form-submitting? (re-frame/subscribe [:ticket-form-submitting?])
        params (reagent/atom {:title ""})]
    (fn []
      [:div
       {:class ["form-group" "column" "col-8"]}
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
         :on-click #(re-frame/dispatch [:event/create-ticket @params])
         :class ["btn" "btn-primary" "btn-lg" "mt-2" "float-right"]}
        "Save"]])))


(defn- create-ticket-page
  []
  (let [page-title (re-frame/subscribe [:page-title])]
    [:div [:h2 @page-title]
     [:a
      {:href (router/path-for :home)}
      "<- Back to list"]
     [:div
      {:class ["columns"]}
      [ticket-form]]]))


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
     {:class ["container" "grid-lg"]}
     [header]
     [content]]))
