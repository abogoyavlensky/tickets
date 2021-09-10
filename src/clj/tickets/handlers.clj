(ns tickets.handlers
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [slingshot.slingshot :refer [throw+]]
            [tickets.components.db :as db-component]
            [tickets.errors :as errors]
            [tickets.util.ring :as ring-util])
  (:import [java.time.format DateTimeFormatter DateTimeParseException]
           [java.time LocalDate]))


(defn tickets-list
  [db]
  (let [tickets (db-component/get-ticket-list db)]
    (ring-util/json-response tickets)))


(def ^:private date-format
  (DateTimeFormatter/ofPattern "yyyy-MM-dd"))


(defn- valid-date?
  [date-str]
  (try
    (LocalDate/parse date-str date-format)
    (catch DateTimeParseException _
      false)))


(s/def ::not-empty-string
  (fn [val]
    (and
      (string? val)
      (boolean (seq (str/trim val))))))

(s/def ::title ::not-empty-string)
(s/def ::description ::not-empty-string)
(s/def ::applicant ::not-empty-string)
(s/def ::executor ::not-empty-string)
(s/def ::completed-at (s/and string? valid-date?))


(s/def ::ticket-new
  (s/keys
    :req-un [::title
             ::description
             ::applicant
             ::executor
             ::completed-at]))


(defn- conform-ticket-data!
  [spec data]
  (when-not (s/valid? spec data)
    (throw+ {:type :params/validation
             :explain-data (s/explain-data spec data)})))


(defn tickets-create
  [db request]
  (let [ticket-data (:params request)
        _ (conform-ticket-data! ::ticket-new ticket-data)
        created-ticket (db-component/create-ticket! db ticket-data)]
    (ring-util/json-response created-ticket)))


; TODO: remove!
(comment
  ;(clojure.core/fn [%] (clojure.core/contains? % :title))
  (let [params {:applicant "hjkfhjk",
                :completed-at "2021-09-",
                ;:description "sdf",
                :executor "fhjfdsd gyudh",
                :title 11}
        explain-data (s/explain-data ::ticket-new params)]
    (errors/explain-data->error-messages explain-data)))
