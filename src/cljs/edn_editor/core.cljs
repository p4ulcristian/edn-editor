(ns edn-editor.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs.reader :as reader :refer [read-string read]]))

;; -------------------------
;; Views


(def edn-atom (atom {}))

(def editor-atom (atom {:ki {:gondolta {:volna "hogy"}}
                        :he {:ja [{:mas "valami"}
                                  {:hello "hey"
                                   :muhaha {:hello [1 2 3]}}]}}))

(defn notification [text]
  (.notification js/UIkit text))

(def show-me-da-way (atom []))


(defn try-to-read [data]
  (if (or (and (= "{" (first data)) (= "}" (last data)))
          (and (= "[" (first data)) (= "]" (last data))))
   (if-let [new-data (read-string data)]
     new-data
     data)
   data))


(defn edit-if-want [[the-key the-content]]
  (let [edit? (atom false)
        the-value (atom "")]
    (fn [[the-key the-content]]
      (if @edit?
         [:div [:input.uk-input  {:on-change #(reset! the-value (-> % .-target .-value))
                                  :default-value (str the-content)}]
          [:a  [:span {:data-uk-icon "check" :on-click #(do
                                                          (swap! editor-atom assoc-in (vec (conj @show-me-da-way the-key))
                                                                 (try-to-read @the-value))
                                                          (reset! edit? false))}]]]
         [:div
          {:on-click #(reset! edit? true)}
          (str the-content)]))))



(defn one-key-and-content [data]
  [:div.uk-grid-collapse.uk-width-1-1 {:data-uk-grid true :style {:overflow-y "scroll !important"}}
   (cond
     (or (map? (second data))
         (vector? (second data))) [:div.uk-width-1-5.uk-card-primary.uk-padding-small.uk-box-shadow-bottom.uk-margin-bottom-large
                                   {:on-click #(reset! show-me-da-way (conj @show-me-da-way (first data)))}
                                   (first data)]
     :else [:div.uk-width-1-5.uk-secondary.uk-padding-small.uk-box-shadow-bottom.uk-margin-bottom-large
            {:on-click #(notification "There is no deeper")}
            (first data)])

   [:div.uk-width-4-5.uk-card-secondary.uk-padding-small
    {:style {:border-bottom "1px solid white"}}
    [edit-if-want data]]])


(defn one-array-item [index data]
  [:div.uk-grid-collapse.uk-width-1-1 {:data-uk-grid true :style {:overflow-y "scroll !important"}}
   (if
     (or (vector? data)
         (map? data))
     [:div.uk-width-1-5.uk-card-primary.uk-padding-small.uk-box-shadow-bottom.uk-margin-bottom-large
      {:on-click #(reset! show-me-da-way (conj @show-me-da-way index))}
      index]
     [:div.uk-width-1-5.uk-card-secondary.uk-padding-small.uk-box-shadow-bottom.uk-margin-bottom-large
      {:on-click #(notification "There is no deeper")}
      index])


   [:div.uk-width-4-5.uk-card-secondary.uk-padding-small
    {:style {:border-bottom "1px solid white"}}
    [:div.uk-text-truncate [edit-if-want [index data]]]]])

(defn walk-edn [the-map]
  [:div
   ;(str the-map)
   (if (vector? (get-in the-map @show-me-da-way))
     (map-indexed #(-> ^{:key %1}[one-array-item %1 %2])
                  (if (= [] @show-me-da-way)
                    the-map
                    (get-in the-map @show-me-da-way)))

     (map-indexed #(-> ^{:key (first %2)}[one-key-and-content %2])
                  (if (= [] @show-me-da-way)
                    the-map
                    (get-in the-map @show-me-da-way))))])

(defn add-edn []
  (let []
    (fn []
      [:div.uk-card.uk-card-secondary
       [:div.uk-card-body.uk-padding-small
        [:form.uk-form.uk-flex-center.uk-margin-remove {:data-uk-grid  true}
         [:textarea.uk-textarea.uk-width-1-1.uk-text-center.uk-margin-remove.uk-padding-remove {:rows 5 :placeholder "ADD EDN" :on-change #(reset! edn-atom (-> % .-target .-value))}]]
        [:button.uk-button.uk-button-secondary.uk-width-1-2.uk-margin-remove {:on-click #(do
                                                                                           (reset! show-me-da-way [])
                                                                                           (reset! editor-atom (read-string @edn-atom)))}
         "Process data!"]]])))


(defn one-breadcrumb [index text]
  [:li [:a {:on-click #(reset! show-me-da-way (vec (take (inc index) @show-me-da-way)))}
        text]])

(defn breadcrumbs []
  [:div.uk-width-1-1
   [:ul.uk-breadcrumb
    [:li [:a {:on-click #(reset! show-me-da-way [])}
          "root"]]
    (map-indexed #(-> ^{:key %1} [one-breadcrumb %1 %2]) @show-me-da-way)]])



(defn home-page []
  [:div.uk-card-secondary
   [:div.uk-container {:style {:min-height "100vh"}}
    [:h2.uk-padding-small.uk-margin-remove.uk-heading-line.uk-text-center.uk-card-secondary {:data-uk-sticky true} [:span "EDN (Extensible Data Notation) Editor"]]
    [add-edn]
    [breadcrumbs]
    [walk-edn @editor-atom]]])
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
