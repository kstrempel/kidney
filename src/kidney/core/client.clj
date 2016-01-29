(ns kidney.core.client
  (:import (java.util UUID))
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer (chan close! pub sub <!! timeout)]))

(defn discover [service]
  ["localhost:8080"])


(defprotocol IClient
  (stop [this])
  (request [this method parameters]))


(defrecord Client [connections]
  IClient

  (stop [this]
    (doseq [[connection ch] connections]
      (.close connection)
      (close! ch)))

  (request [this method parameters]
    (let [message-id (str (UUID/randomUUID))
          [connection ch] (first connections)
          chto (timeout 2000)]
      (sub (pub ch :message-id) message-id chto)
      (.send connection {:method method :parameters parameters :message-id message-id})
      (let [reply (<!! chto)] 
        (:result reply))))
  )


(defn client [service client-factory]
  (log/info "create client")
  (let [endpoints (discover service)
        connections (doall
                     (map #(list (client-factory %1 %2) %1)
                          (repeatedly #(chan))
                          endpoints))]
    (->Client connections)))
