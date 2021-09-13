(ns tickets.handlers
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.instant :as instant]
            [slingshot.slingshot :refer [throw+]]
            [tickets.util.ring :as ring-util]
            [tickets.queries :as queries])
  (:import [java.time.format DateTimeFormatter DateTimeParseException]
           [java.time LocalDate]))


(def ^:private date-format
  (DateTimeFormatter/ofPattern "yyyy-MM-dd"))


(defn- valid-date?
  [date-str]
  (try
    (LocalDate/parse date-str date-format)
    (catch DateTimeParseException _
      false)))


(defn- date->string
  [date]
  (format "%1$tY-%1$tm-%1$td" date))


(s/def ::not-empty-string
  (fn [value]
    (and
      (string? value)
      (boolean (seq (str/trim value))))))


(s/def :ticket/id integer?)
(s/def :ticket/title ::not-empty-string)
(s/def :ticket/description ::not-empty-string)
(s/def :ticket/applicant ::not-empty-string)
(s/def :ticket/executor ::not-empty-string)
(s/def :ticket/completed-at
  (s/and
    string?
    valid-date?
    (s/conformer
        instant/read-instant-date
        date->string)))


(s/def ::ticket-in
  (s/keys
    :req-un [:ticket/title
             :ticket/description
             :ticket/applicant
             :ticket/executor
             :ticket/completed-at]))


(defn- conform-ticket-data!
  [spec data]
  (let [conformed (s/conform spec data)]
    (if (= ::s/invalid conformed)
      (throw+ {:type :params/validation
               :explain-data (s/explain-data spec data)})
      conformed)))


(s/def ::ticket-out
  (s/merge
    ::ticket-in
    (s/keys
      :req-un [:ticket/id])))


(defn tickets-create
  [db request]
  (let [ticket-data (:params request)
        ticket-conformed (conform-ticket-data! ::ticket-in ticket-data)
        created-ticket (queries/create-ticket! db ticket-conformed)]
    (->> created-ticket
         (s/unform ::ticket-out)
         (ring-util/json-response))))


(defn tickets-list
  [db]
  (let [tickets (queries/get-ticket-list db)]
    (->> tickets
         (map #(s/unform ::ticket-out %))
         (ring-util/json-response))))
