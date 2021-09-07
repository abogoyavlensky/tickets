(ns tickets.views
  (:require [re-frame.core :as re-frame]))


(defn main-panel []
  (let [page-title (re-frame/subscribe [:page-title])]
    (fn []
      [:div [:h1 @page-title]
       [:button
        {:on-click #(re-frame/dispatch [:set-page-create-ticket])}
        "Create ticket"]
       [:button
        {:on-click #(re-frame/dispatch [:set-page-home])}
        "Back to list"]])))
