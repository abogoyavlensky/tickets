(ns tickets.views
  (:require [re-frame.core :as re-frame]))

(defn page-title
  [page]
  (case page
    :home "Tickets"
    :create-ticket "Create new ticket"))


(defn main-panel []
  (let [page (re-frame/subscribe [:page])]
    (fn []
      [:div [:h1 (page-title @page)]
       [:button
        {:on-click #(re-frame/dispatch [:set-page-create-ticket])}
        "Create ticket"]
       [:button
        {:on-click #(re-frame/dispatch [:set-page-home])}
        "Back to list"]])))
