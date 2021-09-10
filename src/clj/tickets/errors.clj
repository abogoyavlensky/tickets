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


; TODO: maybe update with multimethods!
(def ^:private error-messages
  {:tickets.handlers/ticket-new
   (fn [problem]
     (let [parsed (s/conform ::parse-contains-pred (:pred problem))]
       (if-not (= ::s/invalid parsed)
         (let [field (get-in parsed [:check :field])
               field-name (get field-names field)]
           {:field field
            :message (str field-name " is required.")}))))
   :tickets.handlers/completed-at (field-error :completed-at "%s value has invalid format.")
   :tickets.handlers/title (field-error :title "%s value should be string.")
   :tickets.handlers/description (field-error :description "%s value should be string.")
   :tickets.handlers/applicant (field-error :applicant "%s value should be string.")
   :tickets.handlers/executor (field-error :executor "%s value should be string.")
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
             :message "Invalid data in form."})))


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
