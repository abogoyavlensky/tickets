(ns tickets.handlers
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.instant :as instant]
            [slingshot.slingshot :refer [throw+]]
            [tickets.components.db :as db-component]
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
        created-ticket (db-component/create-ticket! db #p ticket-conformed)]
    (->> created-ticket
         (s/unform ::ticket-out)
         (ring-util/json-response))))


; TODO: remove!
(comment
  ;(clojure.core/fn [%] (clojure.core/contains? % :title))
  (let [params {:applicant "hjkfhjk",
                :completed-at #inst "2021-09-22",
                :description "sdf",
                :executor "fhjfdsd gyudh",
                :title "some name"}
                ;:id 1111}
        explain-data (s/explain-data ::ticket-new params)]))
    ;(errors/explain-data->error-messages explain-data)))
    ;(s/conform ::ticket-in params)
    ;(s/unform ::ticket-in params)))
