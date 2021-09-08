(ns tickets.util.ring
  (:require [cheshire.core :as json]
            [ring.util.response :as response]))


(defn json-response
  "Return data as ring response in json format."
  [data]
  (-> (response/response (json/generate-string data))
      (response/content-type "application/json")))


(defn json-bad-request
  "Return data as ring response in json format."
  [data]
  (-> (response/bad-request (json/generate-string data))
      (response/content-type "application/json")))
