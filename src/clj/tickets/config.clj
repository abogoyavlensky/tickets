(ns tickets.config
  (:require [environ.core :refer [env]]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.gzip :as gzip]
            [ring.middleware.logger :as logger]
            [ring.middleware.json :as json-params]
            [ring.middleware.keyword-params :as keyword-params]
            [tickets.middlewares :as tickets-middlewares]))


(defn config []
  {:http-port  (Integer. (or (env :port) 8080))
   :db {:db-system "dev"
        :db-name "tickets"}
   :middleware [[defaults/wrap-defaults
                 defaults/api-defaults]
                keyword-params/wrap-keyword-params
                json-params/wrap-json-params
                logger/wrap-with-logger
                gzip/wrap-gzip
                tickets-middlewares/wrap-exceptions]})
