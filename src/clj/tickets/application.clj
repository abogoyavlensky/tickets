(ns tickets.application
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [system.components.endpoint :refer [new-endpoint]]
            [system.components.handler :refer [new-handler]]
            [system.components.middleware :refer [new-middleware]]
            [system.components.jetty :as jetty]
            [tickets.config :as tickets-config]
            [tickets.router :refer [home-routes]]
            [tickets.components.db :as db]
            [tickets.components.server-info :as info]))


(defn app-system
  [config]
  (component/system-map
    :db (db/db-component (:db config))
    :routes (-> (new-endpoint home-routes)
                (component/using [:db]))
    :middleware (new-middleware {:middleware (:middleware config)})
    :handler (-> (new-handler)
                 (component/using [:routes :middleware]))
    :http (-> (jetty/new-jetty :port (:http-port config))
              (component/using [:handler]))
    :server-info (info/server-info (:http-port config))))


(defn -main
  [& _]
  (let [config (tickets-config/config)]
    (-> config
        app-system
        component/start)))
