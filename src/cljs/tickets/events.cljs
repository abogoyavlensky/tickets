(ns tickets.events
  (:require [re-frame.core :as re-frame]
            [tickets.db :as db]))


(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))


(re-frame/reg-event-db
  :set-page-create-ticket
  (fn  [db _]
    (assoc db :name "CREATE PAGE!")))


(re-frame/reg-event-db
  :set-page-home
  (fn  [db _]
    (assoc db :name "HOME PAGE!")))


; TODO: remove!
(comment
  (require '[re-frame.db :as rf-db])
  (swap! rf-db/app-db assoc :name "Interactivity FTW")
  (deref rf-db/app-db))
