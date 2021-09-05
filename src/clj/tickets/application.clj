(ns tickets.application
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [tickets.components.server-info :refer [server-info]]
            [system.components.endpoint :refer [new-endpoint]]
            [system.components.handler :refer [new-handler]]
            [system.components.middleware :refer [new-middleware]]
            [system.components.jetty :refer [new-web-server]]
            [tickets.config :refer [config]]
            [tickets.routes :refer [home-routes]]
            [tickets.components.db :as db]))


(defn app-system [config]
  (component/system-map
    :db (db/db-component)
    :routes     (-> (new-endpoint home-routes)
                    (component/using [:db]))
    :middleware (new-middleware {:middleware (:middleware config)})
    :handler    (-> (new-handler)
                    (component/using [:routes :middleware]))
    :http       (-> (new-web-server (:http-port config))
                    (component/using [:handler]))
    :server-info (server-info (:http-port config))))


(defn -main [& _]
  (let [config (config)]
    (-> config
        app-system
        component/start)))
