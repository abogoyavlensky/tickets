(ns tickets.events
  (:require [re-frame.core :as re-frame]
            [tickets.db :as db]))


(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))


(re-frame/reg-event-db
  :set-current-page
  (fn  [db [_ page]]
    (assoc db :current-page page)))


; TODO: remove!
(comment
  (require '[re-frame.db :as rf-db])
  (swap! rf-db/app-db assoc :name "Some name")
  (deref rf-db/app-db))
