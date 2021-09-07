(ns tickets.views
  (:require [re-frame.core :as re-frame]))


(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div [:h1 "Hello from " @name]
       [:button
        {:on-click #(re-frame/dispatch [:set-page-create-ticket])}
        "Create ticket"]
       [:button
        {:on-click #(re-frame/dispatch [:set-page-home])}
        "Back to list"]])))







;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; TODO: uncomment!
;(defn page-title
;  [page]
;  (case page
;    :home "Tickets"
;    :create-ticket "Create new ticket"))
