(ns tickets.subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :current-page
  (fn [db]
    (:current-page db)))


(re-frame/reg-sub
  :page-title

  (fn [_]
    [(re-frame/subscribe [:current-page])])

  (fn [[current-page] _]
    (case current-page
      :home "Ticket list"
      :create-ticket "Create new ticket")))


(re-frame/reg-sub
  :tickets
  (fn [db]
    (:tickets db)))


(re-frame/reg-sub
  :tickets-error
  (fn [db]
    (:tickets-error db)))


(re-frame/reg-sub
  :tickets-loading?
  (fn [db]
    (:tickets-loading? db)))


(re-frame/reg-sub
  :ticket-form-submitting?
  (fn [db]
    (:ticket-form-submitting? db)))


(re-frame/reg-sub
  :ticket-new-id
  (fn [db]
    (:ticket-new-id db)))


(re-frame/reg-sub
  :ticket-form-errors
  (fn [db]
    (:ticket-form-errors db)))
