(ns autogram.interval
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! put! timeout close! pub sub unsub sliding-buffer poll!]]))

(defonce state (atom {}))

(defn create-loop
  "Takes an input channel, an output channel, and a speed (int).
  ie {:in port :out subscription}"
  [{:keys [in out speed]}]
    (go-loop [interval 0]
      (put! in (if (even? interval) :from :to))
      (<! (timeout speed))
      (if (<! out)
        (recur (inc interval)))))

(defn create-publication [chan]
  (pub chan #(keyword "interval")))

(defn listen
  "Subscribe to the interval.
  If you cannot afford a chan one will be provided for you.
  Returns the channel"
  ([] (listen (chan)))
  ([chan]
    (let [publication (@state :publ)]
      (sub publication :interval chan))))

(defn silence [chan]
  "Unsubscribe from a publication, clearing it's queue.
  Returns the channel"
  (unsub (@state :publ) :interval chan)
  (poll! chan))

(defn stop
  "Close the current channel"
  []
  (let [chan (@state :chan)]
    (close! (@state :chan))
    (poll! chan)))

(defn start
  "Starts the interval, causing it to increment once every second.
  Saves a publication into the state & returns the state"
  ([] (start 1000))
  ([speed]
    (let [ch   (chan)
          publ (create-publication ch)]
      (swap! state assoc-in [:chan] ch)
      (swap! state assoc-in [:publ] publ)
      (create-loop {:in ch :out (listen) :speed speed}))))

(defn restart
  "Close the current channel and open a new one"
  ([] (restart 1000))
  ([speed]
   (stop)
   (start speed)))
