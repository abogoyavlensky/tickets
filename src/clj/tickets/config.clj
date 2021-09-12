(ns tickets.config
  (:require [environ.core :refer [env]]
            [slingshot.slingshot :refer [try+]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [tickets.util.ring :as ring-util]
            [tickets.errors :as errors]))


(defn- error-type->error-code
  [error]
  (str (:type error)))


(defn wrap-exceptions
  [handler]
  (fn [request]
    (try+
      (handler request)
      (catch #(contains? #{:params/validation} (:type %)) e
        (-> {:status :error
             :error-code (error-type->error-code e)
             :errors (errors/explain-data->error-messages (:explain-data e))}
            (ring-util/json-bad-request)))
      (catch [:type :db/error] e
        (-> {:status :error
             :error-code (error-type->error-code e)
             :errors [(:message e)]}
            (ring-util/json-bad-request))))))


(defn config []
  {:http-port  (Integer. (or (env :port) 8080))
   :middleware [[wrap-defaults api-defaults]
                wrap-keyword-params
                wrap-json-params
                wrap-with-logger
                wrap-gzip
                wrap-exceptions]})
