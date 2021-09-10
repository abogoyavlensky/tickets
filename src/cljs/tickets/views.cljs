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


(defn- create-ticket-btn
  []
  [:a
   {:href (router/path-for :create-ticket)
    :class ["btn" "btn-primary" "col-2"]}
   "Create ticket"])


(defn- empty-tickets
  []
  [:div.empty
   [:p.empty-title.h5 "There are no tickets yet."]
   [:p.empty-subtitle "Please create a new ticket."]])


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
      [empty-tickets])))


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
      [create-ticket-btn]]
     [:div
      (if (true? @loading?)
         [:p "Loading..."]
         (if (some? @error)
           [:p @error]
           (render-tickets-table @tickets)))]]))

(defn- error-hint
  [message]
  [:p
   {:class ["form-input-hint"]
    :key message}
   message])

(defn- input-field
  [{:keys [params field label field-type submitting? errors]}]
  (let [field-name-str (name field)
        form-classes ["form-group"]
        form-classes* (if (seq errors)
                        (conj form-classes "has-error")
                        form-classes)]
    [:div
     {:class form-classes*}
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
       :class ["form-input"]}]
     (map error-hint errors)]))



(defn- textarea-field
  [{:keys [params field label submitting? errors]}]
  (let [field-name-str (name field)
        form-classes ["form-group"]
        form-classes* (if (seq errors)
                        (conj form-classes "has-error")
                        form-classes)]
    [:div
     {:class form-classes*}
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
       :class ["form-input"]}]
     (map error-hint errors)]))


(defn- form-error
  [error]
  [:div
   {:class ["toast" "toast-error" "col-12"]}
   error])


(defn- ticket-form
  []
  (let [ticket-form-submitting? (re-frame/subscribe [:ticket-form-submitting?])
        params (reagent/atom {})
        errors (re-frame/subscribe [:ticket-form-errors])]
    (fn []
      [:div
       {:class ["column" "col-8"]}
       (when (seq (:form @errors))
         (map form-error (:form @errors)))
       [input-field {:params params
                     :field :title
                     :label "Title"
                     :field-type "text"
                     :submitting? @ticket-form-submitting?
                     :errors (:title @errors)}]
       [textarea-field {:params params
                        :field :description
                        :label "Description"
                        :submitting? @ticket-form-submitting?
                        :errors (:description @errors)}]
       [input-field {:params params
                     :field :applicant
                     :label "Applicant"
                     :field-type "text"
                     :submitting? @ticket-form-submitting?
                     :errors (:applicant @errors)}]
       [input-field {:params params
                     :field :executor
                     :label "Executor"
                     :field-type "text"
                     :submitting? @ticket-form-submitting?
                     :errors (:executor @errors)}]
       [input-field {:params params
                     :field :completed-at
                     :label "Completion date"
                     :field-type "date"
                     :submitting? @ticket-form-submitting?
                     :errors (:completed-at @errors)}]
       [:button
        {:type :button
         :disabled (true? @ticket-form-submitting?)
         :on-click #(re-frame/dispatch [:event/create-ticket @params])
         :class ["btn" "btn-primary" "btn-lg" "mt-2" "float-right"]}
        "Save"]
       [:a
        {:href (router/path-for :home)
         :class ["btn" "btn-lg" "mt-2" "float-right" "mr-2"]}
        "Cancel"]])))


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
