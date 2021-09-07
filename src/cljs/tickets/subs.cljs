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
