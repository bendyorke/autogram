(ns autogram.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce app-state (atom {:from "Automagic"
                          :to "Autograms"}))

(defn form []
  [:div
    [:h1 "Enter two words to autogram!"]
    (doall (for [element [:from :to]]
      [:input {:style {:padding "10px 20px" :margin 10 :font-size 20}
               :key element
               :type "text"
               :value (@app-state element)
               :on-change
               (fn input-changed [e]
                 (swap! app-state assoc-in [element] (.. e -target -value)))}]))])

(defn letters-in-word [word]
  [:div
    (->> (seq word)
         (map-indexed
           (fn iterate-over-word [i char]
             [:span {:key i} char])))])

(defn magic-area []
  [:div
   [letters-in-word (@app-state :from)]
   [letters-in-word (@app-state :to)]])

(defn autogram []
  [:div
   [form]
   [magic-area]])

(reagent/render-component [autogram]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)