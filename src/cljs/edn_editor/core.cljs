(ns edn-editor.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs.reader :as reader :refer [read-string]]))

;; -------------------------
;; Views


(def edn-atom (atom {}))

(def editor-atom (atom {}))




(defn add-edn []
  (let []
    (fn []
      [:div.uk-container
       (str @edn-atom)
       [:div.uk-card.uk-card-secondary
        [:div.uk-card-body.uk-padding-small
         [:form.uk-form.uk-flex-center.uk-margin-remove {:data-uk-grid  true}
          [:textarea.uk-textarea.uk-width-1-1.uk-text-center.uk-margin-remove.uk-padding-remove {:rows 5 :placeholder "ADD EDN" :on-change #(reset! edn-atom (-> % .-target .-value))}]
          [:button.uk-button.uk-button-secondary.uk-width-1-2.uk-margin-remove "Process data!"]]]]])))


(defn home-page []
  [:div
   [:h2 "Welcome to edn-editor"]
   [add-edn]])
   ;[:div [:input.uk-input.uk-input-default {:placeholder "EDN" :on-change #(reset! edn-atom (-> % .-target .-value))}]]])


;; -------------------------
;; Routes

(defonce page (atom #'home-page))



(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))



;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
