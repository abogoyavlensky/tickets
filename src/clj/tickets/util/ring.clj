(ns tickets.util.ring
  (:require [cheshire.core :as json]
            [ring.util.response :refer [response content-type]]))


(defn json-response
  "Return data as ring response in json format."
  [data]
  (-> (response (json/generate-string data))
      (content-type "application/json")))
