(ns tickets.errors
  (:require [clojure.spec.alpha :as s]))


(def ^:private field-names
  {:title "Title"
   :description "Description"
   :applicant "Applicant"
   :executor "Executor"
   :completed-at "Completion date"})


(s/def ::parse-contains-pred
  (s/cat
    :fn #{'clojure.core/fn}
    :param any?
    :check (s/spec
             (s/cat
               :fn #{'clojure.core/contains?}
               :param any?
               :field keyword?))))

(defn- field-error
  [field message-tmpl]
  {:field (if (some? field) field :form)
   :message (if (some? field)
              (format message-tmpl (field field-names))
              message-tmpl)})


(def ^:private error-messages
  {:tickets.handlers/ticket-in
   (fn [problem]
     (let [parsed (s/conform ::parse-contains-pred (:pred problem))]
       (if-not (= ::s/invalid parsed)
         (let [field (get-in parsed [:check :field])
               field-name (get field-names field)]
           {:field field
            :message (str field-name " is required.")})
         (field-error :form "Form data is invalid."))))
   :ticket/completed-at (field-error :completed-at "%s value has invalid format.")
   :ticket/title (field-error :title "%s value should be string.")
   :ticket/description (field-error :description "%s value should be string.")
   :ticket/applicant (field-error :applicant "%s value should be string.")
   :ticket/executor (field-error :executor "%s value should be string.")
   :tickets.handlers/not-empty-string
   (fn [problem]
     (let [field (peek (:in problem))]
       (field-error field "%s value is empty string.")))})


(defn- problem->error-message
  [{:keys [via] :as problem}]
  (let [last-spec (peek via)
        error-message (get error-messages last-spec)]
    (cond
      (fn? error-message) (error-message problem)
      (map? error-message) error-message
      :else {:field :form
             :message "Form data is invalid."})))


(defn explain-data->error-messages
  [explain-data]
  (->> explain-data
      ::s/problems
      (map problem->error-message)
      (group-by :field)
      (reduce-kv
        (fn [m k v]
          (assoc m k (mapv :message v)))
        {})))
