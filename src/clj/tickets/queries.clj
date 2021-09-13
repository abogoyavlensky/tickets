(ns tickets.queries
  (:require [clojure.core.async :as async]
            [datomic.client.api.async :as d]
            [cognitect.anomalies :as anomalies]
            [slingshot.slingshot :refer [throw+]])
  (:import [java.util UUID]))


(def ^:private QUERY-TIMEOUT 5000)  ; 5 second


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
        tx (async-db-query!
             #(d/transact (:conn db) {:tx-data [ticket-qualified]}))
        actual-id (get-in tx [:tempids temp-id])]
    (assoc ticket-data :id actual-id)))


(defn get-ticket-list
  "Return list of tickets from db."
  [db options]
  (let [query '[:find ?e ?title ?description ?applicant ?executor ?completed-at
                :keys id title description applicant executor completed-at
                :where [?e :ticket/title ?title]
                [?e :ticket/description ?description]
                [?e :ticket/applicant ?applicant]
                [?e :ticket/executor ?executor]
                [?e :ticket/completed-at ?completed-at]]]
    (async-db-query!
      #(d/q {:query query
             :args [(d/db (:conn db))]
             :limit (:limit options)
             :offset (:offset options)}))))


; TODO: remove!
(comment
  (require '[reloaded.repl :refer [system]])
  (let [db (:db system)]
    (get-ticket-list db {})))
