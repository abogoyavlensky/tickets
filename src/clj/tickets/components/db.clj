(ns tickets.components.db
  (:require [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [slingshot.slingshot :refer [throw+]])
  (:import [java.util UUID]))


(defn uuid
  []
  (str (UUID/randomUUID)))

(s/def ::title string?)
(s/def ::description string?)
(s/def ::applicant string?)
(s/def ::executor string?)
; TODO: validate as date, and conform probably?!
(s/def ::completed-at string?)

(s/def ::ticket-new
  (s/keys
    :req-un [::title
             ::description
             ::applicant
             ::executor
             ::completed-at]))


(defn check-data!
  [spec data]
  (when-not (s/valid? spec data)
    (throw+ {:type :params/validation
             :message (s/explain-str spec data)})))


(defn create-ticket!
  "Create new ticket with given data."
  [db ticket-data]
  (check-data! ::ticket-new ticket-data)
  (let [ticket-data* (assoc ticket-data :id (uuid))]
    (swap! (:conn db) update :tickets conj ticket-data*)))


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
                                             :completed-at "20210-08-09"}
                                            {:id (uuid)
                                             :title "Second ticket"
                                             :description (str "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
                                                               "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                                                               " Ut enim ad minim veniam, quis nostrud exercitation"
                                                               " ullamco laboris nisi ut aliquip ex ea commodo consequat.")
                                             :applicant "User Sender 2"
                                             :executor "Employer Executor 2"
                                             :completed-at "20210-08-22"}]})))
  (stop [component]
    (dissoc component :conn)))


(defn db-component []
  (->DB))
