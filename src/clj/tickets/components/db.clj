(ns tickets.components.db
  (:require [com.stuartsierra.component :as component]
            [clojure.instant :as instant]
            [clojure.core.async :as async]
            [datomic.client.api.async :as d]
            [datomic.dev-local :as dev-local]
            [cognitect.anomalies :as anomalies]
            [slingshot.slingshot :refer [throw+]])
  (:import [java.util UUID]))


(def ^:private DB-NAME "tickets")
(def ^:private QUERY-TIMEOUT 5000)  ; 5 second


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


(defrecord DB []
  component/Lifecycle
  (start [component]
    (println "Running db connection...")
    (let [client (d/client {:server-type :dev-local
                            :system "dev"
                            :storage-dir :mem})
          _ (async/<!! (d/create-database client {:db-name "tickets"}))
          conn (async/<!! (d/connect client {:db-name DB-NAME}))]
      (d/transact conn {:tx-data db-schema})
      (-> component
          (assoc :client client)
          (assoc :conn conn))))
  (stop [component]
    (let [{:keys [client conn]} component]
      (when (every? some? [client conn])
        (dev-local/release-db {:system (:system db-config)
                               :db-name DB-NAME})
        (d/delete-database client {:db-name DB-NAME}))
      (dissoc component :client :conn))))


  ; TODO: add ability to reset DB component!


(defn- uuid
  []
  (str (UUID/randomUUID)))


(defn- kw->qualified-kw
  [ns-kw kw]
  (keyword (name ns-kw) (name kw)))


(defn- map->qualified-map
  "Turn map's keyword keys to qualified keywords."
  [ns-kw data]
  (reduce-kv
    (fn [m k v]
      (assoc m (kw->qualified-kw ns-kw k) v))
    {}
    data))


; Public

(defn db-component []
  (->DB))

(defn- async-db-query!
  "Perform async db query and return result or throw exception otherwise."
  [query-async-fn]
  (let [result-ch (query-async-fn)
        [result channel] (async/alts!! [result-ch (async/timeout QUERY-TIMEOUT)])]
    (when-not (= result-ch channel)
      (throw+ {:type :db/error
               :error-code :query-timeout
               :message "Database query exceeded timeout."}))
    (if (:error result)
      (throw+ {:type :db/error
               :error-code (::anomalies/category result)
               :error result
               :message (::anomalies/message result)})
      result)))

(defn create-ticket!
  "Create new ticket with given data."
  [db ticket-data]
  (let [temp-id (uuid)
        ticket-qualified (-> (map->qualified-map :ticket ticket-data)
                             (assoc :db/id temp-id))
        tx (async-db-query! #(d/transact (:conn db) {:tx-data [ticket-qualified]}))
        actual-id (get-in tx [:tempids temp-id])]
    (assoc ticket-data :id actual-id)))


(defn get-ticket-list
  "Return list of tickets from db."
  [db]
  (let [query '[:find ?e ?title ?description ?applicant ?executor ?completed-at
                :keys id title description applicant executor completed-at
                :where [?e :ticket/title ?title]
                       [?e :ticket/description ?description]
                       [?e :ticket/applicant ?applicant]
                       [?e :ticket/executor ?executor]
                       [?e :ticket/completed-at ?completed-at]]]
    (async-db-query! #(d/q {:query query :args [(d/db (:conn db))]}))))



; TODO: remove!
(comment
  (require '[reloaded.repl :refer [system]])
  (let [data [{:ticket/title "New ticket 1"
               :ticket/description "Some important ticket!"
               :ticket/applicant "User Appl 1"
               :ticket/executor "User Exec 1"
               :ticket/completed-at #inst "2021-09-22"}
              {:ticket/title "New ticket 2"
               :ticket/description "Sdjsdfj sdjlsjdjs sjdfksjdfj."
               :ticket/applicant "User Appl 2"
               :ticket/executor "User Exec 2"
               :ticket/completed-at #inst "2018-11-03"}]
        db (get-in system [:db])
        conn (get-in system [:db :conn])
        query '[:find ?e ?title ?description ?applicant ?executor ?completed-at
                :keys id title description applicant executor completed-at
                :timeout 100
                :where [?e :ticket/title ?title]
                       [?e :ticket/description ?description]
                       [?e :ticket/applicant ?applicant]
                       [?e :ticket/executor ?executor]
                       [?e :ticket/completed-at ?completed-at]]
        ticket-data {:title "SECOND ticket"
                     :description (str "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
                                       "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                                       " Ut enim ad minim veniam, quis nostrud exercitation"
                                       " ullamco laboris nisi ut aliquip ex ea commodo consequat.")
                     :applicant "User Sender"
                     :executor "Employer Executor"
                     :completed-at (instant/read-instant-date "2021-09-08")}]
    ;(create-ticket! db ticket-data)))
    (get-ticket-list db)))


    ;(def RES (create-ticket! db ticket-data))))
    ;(d/transact conn {:tx-data data
    ;                  :timeout 0.1})
    ;(async/<!! (d-async/transact conn {:tx-data [(map->qualified-map :ticket ticket-data)]
    ;                                   :timeou 0}))))

    ;(d/q query (d/db conn))))
    ;(async/<!! (d-async/q {:query query :args [(d/db conn)]}))))
    ;(d-async/q {:query query :args [(d/db conn)]})))

    ;(async/<!! (async/go
    ;             (let [res-ch (d-async/q {:query query :args [(d/db conn)]})]
    ;               (async/<! res-ch))))))

    ;(let [res-ch (d/q {:query query :args [(d/db conn)]})
    ;      [items channel] (async/alts!! [res-ch (async/timeout 0)])]
    ;  ;(prn items)
    ;  ;(prn channel)
    ;  (if (= res-ch channel)
    ;    items
    ;    (prn "Timed out!")))))


    ;    date (-> (d/q query (d/db conn)) second :completed-at)]))
    ;(.format (SimpleDateFormat. "MM/dd/yyyy") date)))
    ;(.format date "MM/dd/yyyy")))
    ;(format "%1$tY-%1$tm-%1$td" date)))
    ;(->> (get-ticket-list db))))
    ;     first
    ;     (reduce-kv
    ;       (fn [m k v] (assoc m (namespaced-kw :ticket k) v))
    ;       {})
    ;     (map->qualified-map :ticket)
    ;     (qualified-map->map))))
    ;(keyword "ticket" "title")))
    ;RES))
    ;(d/resolve-tempid (d/db conn) (:tempids RES) (d/tempid :db.part/user -200))))
