(ns edn-editor.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
  ;[clojure.edn/reader :refer [read-string]]
            [cljs.reader :as reader :refer [read-string read]]))

;; -------------------------
;; Views


(def edn-atom (atom {}))

(def editor-atom (atom {:all {
                              :contacts [{:id 1 :name "Batman" :tags ["black" "vigilante" "rich"]}
                                         {:id 2 :name "Superman" :tags ["strong" "alien" "cryptonite"]}
                                         {:id 3 :name "Flash" :tags ["fast" "sonic-in-red" "lighting"]}]}}))


(defn notification [text]
  (.notification js/UIkit text))

(def show-me-da-way (atom []))


(defn try-to-read [data]
  (if (or (and (= "{" (first data)) (= "}" (last data)))
          (and (= "[" (first data)) (= "]" (last data))))
    (try
      (read-string data)
      (catch :default e
        data))
    ;(finally data))

    data))


(defn edit-if-want [[the-key the-content]]
  (let [edit? (atom false)
        the-value (atom the-content)]
    (fn [[the-key the-content]]
      (if @edit?
        [:div.uk-grid-collapse {:data-uk-grid true}
         [:button.uk-button-default.uk-button-small.uk-button.uk-text-center.uk-padding-small.uk-padding-remove-vertical
          {:data-uk-icon "check" :on-click #(do
                                              (swap! editor-atom assoc-in the-key
                                                     (try-to-read @the-value))
                                              (reset! edit? false))}]
         [:textarea.uk-textarea.uk-width-expand
          {:rows 6
           :on-change     #(reset! the-value (-> % .-target .-value))
           :default-value (str the-content)}]]

        [:div.uk-inline.uk-width-1-1
         {:on-click #(reset! edit? true)}
         (str the-content)
         [:span.uk-label.uk-label-danger.uk-position-right (cond
                                                            (map? (get-in @editor-atom (vec (conj @show-me-da-way the-key))))
                                                            "map"
                                                            (vector? (get-in @editor-atom (vec (conj @show-me-da-way the-key))))
                                                            "vector"
                                                            :else "string")]]))))






(defn one-key-and-content [data]
  [:div.uk-grid-collapse.uk-width-1-1 {:data-uk-grid true :style {:overflow-y "scroll !important"}}
   (cond
     (or (map? (second data))
         (vector? (second data))) [:div.uk-width-1-5.uk-card-primary.uk-padding-small.uk-box-shadow-bottom.uk-margin-bottom-large
                                   {:on-click #(reset! show-me-da-way (conj @show-me-da-way (first data)))}
                                   (first data)]
     :else [:div.uk-width-1-5.uk-secondary.uk-padding-small.uk-box-shadow-bottom.uk-margin-bottom-large
            {:on-click #(notification "There is no further")}
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
      {:on-click #(notification "There is no further")}
      index])


   [:div.uk-width-4-5.uk-card-secondary.uk-padding-small
    {:style {:border-bottom "1px solid white"}}
    [:div.uk-text-truncate [edit-if-want [index data]]]]])





(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))



(defn recursion-wrapper [the-item this-data next-item route the-map]
  (let [edit? (atom false)
        show? (atom false)]
    (fn [the-item this-data next-item route  the-map]
      [:div.uk-width-1-1.uk-inline
       [:label.uk-label {:on-click (fn [a] (do
                                             (reset! show? (not @show?))))}
        this-data]
       (if @edit?
         [:div
          [:span.uk-padding-small.uk-padding-remove-vertical
           {:on-click #(reset! edit? (not @edit?))
            :data-uk-icon "file-edit"}]
          [edit-if-want [route next-item]]]
         (if (or (map? next-item) (vector? next-item))
           [:span.uk-padding-small.uk-padding-remove-vertical
            {:on-click #(reset! edit? (not @edit?))
             :data-uk-icon "file-edit"}]))
       [:span.uk-padding-small.uk-position-right
        [:span.uk-label {:style {:background "lightblue"}}
         (str (cond
                (map? (get-in @editor-atom route))
                "map"
                (vector? (get-in @editor-atom route))
                "vector"
                (string? (get-in @editor-atom route))
                "string"
                (number? (get-in @editor-atom route))
                "number"
                :else "más"))]]
       [:div
        {:style {:display (if @show? "" "none")}}
        the-item]])))


(defn walk-edn [the-map route]
  (let []
    (fn [the-map route]
      [:ul.uk-margin-remove.uk-list
       (if (or (map? the-map) (vector? the-map))

         (map-indexed #(-> ^{:key %1}[:li
                                      (cond
                                        (map? the-map)
                                        ^{:key 1}[recursion-wrapper
                                                  [walk-edn (second %2) (conj route (first %2))]
                                                  (str (first %2))
                                                  (second %2)
                                                  (conj route (first %2))
                                                  the-map]
                                        (vector? the-map)
                                        ^{:key 2}[recursion-wrapper
                                                  [walk-edn %2
                                                   (conj route %1)]
                                                  (str %1)
                                                  %2
                                                  (conj route %1)
                                                  the-map]
                                        :else ^{:key 3}[:div "Vége"])])

                      (cond
                        (map? the-map)
                        (sort-by #(str (first %))
                                 the-map)
                        :else the-map))
         [:span
          [edit-if-want [route the-map]]])])))


(defn add-edn []
  (let []
    (fn []
      [:div.uk-card.uk-card-secondary
       [:div.uk-card-body.uk-padding-small
        [:form.uk-form.uk-flex-center.uk-margin-remove {:data-uk-grid true}
         [:textarea.uk-textarea.uk-width-1-1.uk-text-center.uk-margin-remove.uk-padding-remove {:rows 5 :placeholder "ADD EDN" :on-change #(reset! edn-atom (-> % .-target .-value))}]]
        [:button.uk-button.uk-button-secondary.uk-width-1-1.uk-margin-remove {:on-click #(reset! editor-atom {:all (read-string @edn-atom)})}
         "Process data!"]]])))

(defn result []
  (let [shown? (atom false)]
    (fn []
      [:div.uk-width-1-1.uk-margin-large
       (if @shown?
         [:textarea.uk-textarea.uk-height-large {:value (str (:all @editor-atom)) :disabled true}]
         [:button.uk-button.uk-button-default.uk-width-1-1.uk-text-center {:on-click #(reset! shown? true)} "Show Result!"])])))

(defn home-page []
  [:div.uk-card-secondary
   [:div.uk-container {:style {:min-height "100vh"}}
    [:h2.uk-padding-small.uk-margin-remove.uk-heading-line.uk-text-center.uk-card-secondary {:data-uk-sticky true} [:span "EDN (Extensible Data Notation) Editor"]]
    [add-edn]
    [walk-edn @editor-atom []]
    [result]]])
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
