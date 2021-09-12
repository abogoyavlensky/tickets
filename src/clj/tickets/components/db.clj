(ns tickets.components.db
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [datomic.client.api.async :as d]
            [datomic.dev-local :as dev-local]
            [slingshot.slingshot :refer [throw+]]))


(def ^:private db-config
  {:server-type :dev-local
   :system "dev"
   :storage-dir :mem})


(def ^:private db-schema
  [{:db/ident :ticket/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the ticket"}
   {:db/ident :ticket/description
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The description of the ticket"}
   {:db/ident :ticket/applicant
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The applicant name"}
   {:db/ident :ticket/executor
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The executor name"}
   {:db/ident :ticket/completed-at
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "The date of ticket completion"}])


(defrecord DB [db-system db-name]
  component/Lifecycle
  (start [component]
    (println "Running db connection...")
    (let [client (d/client {:server-type :dev-local
                            :system db-system
                            :storage-dir :mem})
          _ (async/<!! (d/create-database client {:db-name db-name}))
          conn (async/<!! (d/connect client {:db-name db-name}))]
      (d/transact conn {:tx-data db-schema})
      (-> component
          (assoc :client client)
          (assoc :conn conn))))
  (stop [component]
    (let [{:keys [client conn]} component]
      (when (every? some? [client conn])
        (dev-local/release-db {:system (:system db-config)
                               :db-name db-name
                               :storage-dir :mem})
        (d/delete-database client {:db-name db-name}))
      (dissoc component :client :conn))))


; Public

(defn db-component
  [{:keys [db-system db-name]}]
  (map->DB {:db-system db-system
            :db-name db-name}))
