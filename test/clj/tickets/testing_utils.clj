(ns tickets.testing-utils
  (:require [com.stuartsierra.component :as component]
            [system.components.middleware :refer [new-middleware]]
            [ring.middleware.file :refer [wrap-file]]
            [tickets.config :as tickets-config]
            [tickets.application :as application]))


(def ^:private TEST-PORT 8081)
(def TEST-URL-BASE (str "http://localhost:" TEST-PORT))

(def TEST-URL-API (str TEST-URL-BASE "/api"))
(def TEST-URL-API-TICKETS (str TEST-URL-API "/tickets"))

(def TEST-URL-FRONT-TICKETS (str TEST-URL-BASE "/testapp"))

(def ^:dynamic *test-system* nil)


(defn create-test-system
  []
  (let [config (-> (tickets-config/config)
                   (assoc :http-port TEST-PORT)
                   (assoc-in [:db :db-system] "test")
                   (assoc-in [:db :db-name] "tickets-test"))]
    (-> (application/app-system config)
        (assoc :middleware (new-middleware
                             {:middleware (into [[wrap-file "dev-target/public"]]
                                                (:middleware config))})))))


(defn fixture-system
  [test-fn]
  (binding [*test-system* (component/start (create-test-system))]
    (try
      (test-fn)
      (finally
        (component/stop *test-system*)))))
