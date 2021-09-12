(ns tickets.middlewares
  (:require [slingshot.slingshot :refer [try+]]
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

