(ns tickets.components.db
  (:require [com.stuartsierra.component :as component])
  (:import [java.util UUID]))

(defn uuid
  []
  (str (UUID/randomUUID)))


(defn create-ticket!
  [db ticket-data]
  (swap! (:conn db) conj ticket-data))


(defn get-ticket-list
  [db]
  (get @(:conn db) :tickets))


(defrecord DB []
  component/Lifecycle
  (start [component]
    (println "Running db connection pool...")
    (assoc component :conn (atom {:tickets [{:id (uuid)
                                             :title "First ticket"
                                             :applicant "User Sender"
                                             :executor "Employer Executor"
                                             :completed-at "20210-08-09"}
                                            {:id (uuid)
                                             :title "Second ticket"
                                             :applicant "User Sender 2"
                                             :executor "Employer Executor 2"
                                             :completed-at "20210-08-22"}]})))
  (stop [component]
    (dissoc component :conn)))


(defn db-component []
  (->DB))
