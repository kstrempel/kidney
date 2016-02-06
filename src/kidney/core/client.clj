(ns kidney.core.client
  (:import (java.util UUID)
           (kidney.core.exceptions Timeout RemoteError))
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
          chto (timeout 500)]
      (sub (pub ch :message-id) message-id chto)
      (.send connection {:method method :parameters parameters :message-id message-id})
      ;; when reply is nil timeout exception
      (if-let [reply (<!! chto)]
        (if-let [exception (:exception reply)]
          (throw (RemoteError. (str (:message reply))))
          (:result reply))
        (throw (Timeout. (str "Timeout of message " message-id)))
        )))
  )


(defn client [service client-factory]
  (log/info "create client")
  (let [endpoints (discover service)
        connections (doall
                     (map #(list (client-factory %1 %2) %1)
                          (repeatedly #(chan))
                          endpoints))]
    (->Client connections)))
