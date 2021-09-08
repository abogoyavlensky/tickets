(ns tickets.events
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            ; import http-fx to register events
            [day8.re-frame.http-fx]
            [tickets.db :as db]
            [tickets.router :as router]))


(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))


(re-frame/reg-event-fx
  :get-tickets
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc :tickets-loading? true)
             (assoc :tickets-error nil))
     :http-xhrio {:method          :get
                  :uri             (router/path-for-api :api-tickets-list)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:get-tickets-success]
                  :on-failure      [:get-tickets-error]}}))


(re-frame/reg-event-db
  :get-tickets-success
  (fn [db [_ tickets]]
    (-> db
        (assoc :tickets tickets)
        (assoc :tickets-loading? false))))


(re-frame/reg-event-db
  :get-tickets-error
  (fn [db [_ _]]
    (-> db
        (assoc :tickets-error (str "Error happened while fetching tickets. "
                                   "Please try to reload the page."))
        (assoc :tickets-loading? false))))


(re-frame/reg-event-fx
  :set-current-page
  (fn  [{:keys [db]} [_ page]]
    (let [state {:db (assoc db :current-page page)}]
      (case page
        :home (assoc state :dispatch [:get-tickets])
        state))))



; TODO: remove!
(comment
  (require '[re-frame.db :as rf-db])
  (swap! rf-db/app-db assoc :name "Some name")
  (deref rf-db/app-db))
