(ns tickets.components.db
  (:require [com.stuartsierra.component :as component])
  (:import [java.util UUID]))


(defn- uuid
  []
  (str (UUID/randomUUID)))


(defn create-ticket!
  "Create new ticket with given data."
  [db ticket-data]
  (let [ticket-data* (assoc ticket-data :id (uuid))]
    (swap! (:conn db) update :tickets conj ticket-data*)
    ticket-data*))


(defn get-ticket-list
  "Return list of tickets from db."
  [db]
  (get @(:conn db) :tickets))


(defrecord DB []
  component/Lifecycle
  (start [component]
    (println "Running db connection pool...")
    ; TODO: add Datomic!
    (assoc component :conn (atom {:tickets [{:id (uuid)
                                             :title "First ticket"
                                             :description (str "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
                                                               "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                                                               " Ut enim ad minim veniam, quis nostrud exercitation"
                                                               " ullamco laboris nisi ut aliquip ex ea commodo consequat.")
                                             :applicant "User Sender"
                                             :executor "Employer Executor"
                                             :completed-at "2021-09-08"}
                                            {:id (uuid)
                                             :title "Second ticket"
                                             :description (str "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
                                                               "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                                                               " Ut enim ad minim veniam, quis nostrud exercitation"
                                                               " ullamco laboris nisi ut aliquip ex ea commodo consequat.")
                                             :applicant "User Sender 2"
                                             :executor "Employer Executor 2"
                                             :completed-at "2021-08-22"}]})))
  (stop [component]
    (dissoc component :conn)))


(defn db-component []
  (->DB))
