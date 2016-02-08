(ns autogram.interval
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! put! timeout close! pub sub unsub sliding-buffer]]))

(defonce state (atom {}))

(defn create-loop
  "Takes an input channel and an output channel.
  ie {:in port :out subscription}"
  [{:keys [in out]}]
    (go-loop [interval 0]
      (put! in (if (even? interval) :from :to))
      (prn "Loop" (if (even? interval) :from :to))
      (<! (timeout 1000))
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
  (go (<! chan)))

(defn stop
  "Close the current channel"
  []
  (close! (@state :chan)))

(defn start
  "Starts the interval, causing it to increment once every second.
  Saves a publication into the state & returns the state"
  []
  (let [ch   (chan)
        publ (create-publication ch)]
    (swap! state assoc-in [:chan] ch)
    (swap! state assoc-in [:publ] publ)
    (create-loop {:in ch :out (listen)})))

(defn restart
  "Close the current channel and open a new one"
  []
  (stop)
  (start))
