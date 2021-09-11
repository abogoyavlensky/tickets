(ns tickets.db
  (:require [datomic.client.api :as d]
            [datomic.dev-local :as dev-local]))


(def client (d/client {:server-type :dev-local
                       :system "dev"
                       :storage-dir :mem}))

(def conn (d/connect client {:db-name "tickets"}))


(def db (d/db conn)


;(def db-schema
;  [{:db/ident :ticket/title
;    :db/valueType :db.type/string
;    :db/cardinality :db.cardinality/one
;    :db/doc "The title of the ticket"}
;   {:db/ident :ticket/description
;    :db/valueType :db.type/string
;    :db/cardinality :db.cardinality/one
;    :db/doc "The description of the ticket"}])


;(comment
;  (d/create-database client {:db-name "tickets"})
;  (d/transact conn {:tx-data db-schema})
;  (dev-local/release-db {:system "dev"
;                         :db-name "tickets"})
;  (d/delete-database client {:db-name "tickets"})
  (let [data [{:ticket/title "New ticket 1"
               :ticket/description "Some important ticket!"}
              {:ticket/title "New ticket 2"
               :ticket/description "Sdjsdfj sdjlsjdjs sjdfksjdfj."}]
        query '[:find ?ticket-title
                :where [_ :ticket/title ?ticket-title]]]
    ;(d/transact conn {:tx-data data})))
    (d/q query db)))
