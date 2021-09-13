(ns tickets.handlers
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.instant :as instant]
            [slingshot.slingshot :refer [throw+]]
            [tickets.util.ring :as ring-util]
            [tickets.queries :as queries])
  (:import [java.time.format DateTimeFormatter DateTimeParseException]
           [java.time LocalDate]))

(def ^:private PAGE-SIZE 2)

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
  (fn [val]
    (and
      (string? val)
      (boolean (seq (str/trim val))))))


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
  [db request]
  (let [page (or (Integer/parseInt (get-in request [:params :page])) 1)
        offset (* (dec #p page) PAGE-SIZE)
        limit (+ offset PAGE-SIZE 1)
        tickets (queries/get-ticket-list db
                  {:offset #p offset
                   :limit #p limit})
        has-next-page? (= limit (count tickets))
        tickets* (if has-next-page?
                   (pop tickets)
                   tickets)]
    (->> {:tickets (map #(s/unform ::ticket-out %) tickets*)
          :next-page (when has-next-page?
                       (inc page))
          :prev-page (dec page)}
         (ring-util/json-response))))
