(ns tickets.events
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            ;; import http-fx to register events
            [day8.re-frame.http-fx]
            [tickets.db :as db]
            [tickets.router :as router]))


(re-frame/reg-event-db
  :event/initialize-db
  (fn [_ _]
    db/default-db))


(re-frame/reg-fx
  :fx/set-url
  (fn [{:keys [url]}]
    (router/set-token! url)))


(re-frame/reg-event-fx
  :event/get-tickets
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc :tickets-loading? true)
             (assoc :tickets-error nil))
     :http-xhrio {:method :get
                  :uri (router/path-for-api :api-tickets-list)
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:event/get-tickets-success]
                  :on-failure [:event/get-tickets-error]}}))


(re-frame/reg-event-db
  :event/get-tickets-success
  (fn [db [_ tickets]]
    (-> db
        (assoc :tickets tickets)
        (assoc :tickets-loading? false))))


(re-frame/reg-event-db
  :event/get-tickets-error
  (fn [db [_ _]]
    (-> db
        (assoc :tickets-error (str "Error happened while fetching tickets. "
                                   "Please try to reload the page."))
        (assoc :tickets-loading? false))))


(re-frame/reg-event-fx
  :event/set-current-page
  (fn [{:keys [db]} [_ page]]
    (let [state {:db (-> db
                         (assoc :current-page page)
                         (assoc :ticket-form-errors nil))}]
      (case page
        :home (assoc state :dispatch [:event/get-tickets])
        state))))


(re-frame/reg-event-fx
  :event/create-ticket
  (fn [{:keys [db]} [_ params]]
    {:db (-> db
             (assoc :ticket-form-submitting? true)
             (assoc :ticket-form-errors nil))
     :http-xhrio {:method :post
                  :uri (router/path-for-api :api-tickets-list)
                  :format (ajax/json-request-format)
                  :params params
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [:event/create-ticket-success]
                  :on-failure [:event/create-ticket-error]}}))


(re-frame/reg-event-fx
  :event/create-ticket-success
  (fn [{:keys [db]} [_ response]]
    {:db (-> db
             (assoc :ticket-form-submitting? false)
             (assoc :ticket-form-errors nil)
             (assoc :ticket-new-id (:id response)))
     :fx/set-url {:url (router/path-for :home)}}))


(re-frame/reg-event-db
  :event/create-ticket-error
  (fn [db [_ {{:keys [errors]} :response}]]
    (-> db
        (assoc :ticket-form-submitting? false)
        (assoc :ticket-form-errors errors))))


;; Inspect app-db state
(comment
  (require '[re-frame.db :as rf-db])
  (swap! rf-db/app-db assoc :name "Some name")
  (deref rf-db/app-db))
