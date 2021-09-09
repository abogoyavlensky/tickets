(ns tickets.db)

(def default-db
  {:current-page nil
   :tickets []
   :tickets-error nil
   :tickets-loading? false
   :ticket-form-submitting? false
   :ticket-form-errors nil
   :ticket-new-id nil})
