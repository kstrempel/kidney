(ns kidney.core.client
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer (chan close! pub sub <!!)]))

(defn discover [service]
  ["localhost:8080"])


(defprotocol IClient
  (stop [this])
  (request [this method parameters]))


(defrecord Client [ch endpoints]
  IClient

  (stop [this]
    (doseq [connection endpoints]
      (.close connection))
    (close! ch))

  (request [this method parameters]
    (.send (first endpoints) {:method method :parameters parameters})
    (let [reply (<!! ch)]
      (:message reply))))


(defn client [service client-factory]
  (log/info "create client")
  (let [ch (chan)
        endpoints (discover service)
        connections (doall (map #(client-factory ch %) endpoints))
        c (->Client ch connections)]
    c))
