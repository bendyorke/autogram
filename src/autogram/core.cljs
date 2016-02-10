(ns autogram.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<!]]
            [clojure.set :refer [map-invert]]
            [autogram.interval :as intv]
            [autogram.associate :refer [associate]]))

(enable-console-print!)

(def speed 1500)
(def anim-speed (/ speed 1))

(defonce app-state (atom {:from "Automagic"
                          :to "Autograms"
                          :letter-assocs {}
                          :letters {}
                          :interval (intv/start speed)
                          :current :from}))

(defn transition-to
  "Pass either :to or :from to transition to... to
  Keeps the state transition logic out of the loop"
  [state]
  (if (some #(= state %) '(:to :from))
    (swap! app-state assoc-in [:current] state)))

(defn merge-letter [letters letter key index]
  (let [letter-assocs  (@app-state :letter-assocs)
        letter-assocs  (if (= key :from)
                         letter-assocs
                         (map-invert letter-assocs))
        assoc-index    (letter-assocs index)
        letter-keyword (if (= key :from)
                         (keyword (str index "-" assoc-index))
                         (keyword (str assoc-index "-" index)))]
    (update-in letters [letter-keyword] merge letter)))

(defn calculate-commons [from to]
  (swap! app-state assoc-in [:letters] {})
  (swap! app-state assoc-in [:letter-assocs] (associate from to)))

(defn calculate-position [key char i node]
  (let [node-left    (.. node -offsetLeft)
        parent-width (.. node -parentElement -offsetWidth)
        position     (/ node-left parent-width .01)
        letter       (hash-map :value char key position)]
    (swap! app-state update-in [:letters] merge-letter letter key i)))

(defn form []
  [:div
    [:h1 "Enter two words to autogram!"]
    (doall (for [element [:from :to]]
      ^{:key element}
      [:input {:style {:padding "10px 20px" :margin 10 :font-size 20}
               :type "text"
               :value (@app-state element)
               :on-change
               (fn input-changed [e]
                 (swap! app-state assoc-in [element] (.. e -target -value)))}]))])

(defn letter [key char i]
  (reagent/create-class
    {:component-did-mount #(calculate-position key char i (reagent/dom-node %))
     :reagent-render (fn [_ char]
       [:span char])}))

(defn letters-in-word [key word]
  [:span {:style {:font-size 40
                  :visibility "hidden"
                  :position "absolute"
                  :right "100vw"}}
    (->> (seq word)
         (map-indexed
           (fn iterate-over-word [i char]
             ^{:key (str key char i word)} [letter key char i])))])

(defn position-words [from to]
  (calculate-commons from to)
  [:div
   [letters-in-word :from from]
   [letters-in-word :to to]])

(defn autogram [letters current current-word]
  [:span {:style {:position "relative"
                  :font-size 24}}
   [:span {:style {:visibility "hidden"}} current-word]
   (->> letters
        (map
          (fn [[key {:keys [value] position current}]]
            ^{:key key}
            [:div {:style {:position "absolute"
                           :top 0
                           :left (str position "%")
                           :opacity (if position 1 0)
                           :transition (str "all " anim-speed "ms ease")}}
              value])))])

(defn page []
  [:div
   [form]
   [position-words (@app-state :from) (@app-state :to)]
   [autogram (@app-state :letters) (@app-state :current) (@app-state (@app-state :current))]])

(reagent/render-component [page]
                          (. js/document (getElementById "app")))

(def transition-chan
  (let [chan (intv/listen)]
    (go-loop [state (<! chan)]
      (if state
        (do (transition-to state)
            (recur (<! chan)))))
    chan))

(defn on-js-reload []
  (intv/silence transition-chan)
  (intv/restart speed)
  (intv/listen transition-chan))

