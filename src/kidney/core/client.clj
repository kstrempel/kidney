(ns kidney.core.client
  (:import (java.util UUID)
           (kidney.core.exceptions Timeout RemoteError))
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer (chan close! pub sub <!! timeout)]))

(defn discover [service]
  ["localhost:9999"])


(defprotocol IClient
  (stop [this])
  (request [this method parameters]))


(defrecord Client [connections]
  IClient

  (stop [this]
    (log/info "stop client")
    (doseq [[connection ch] connections]
      (.close connection)))

  (request [this method parameters]
    (let [message-id (str (UUID/randomUUID))
          [connection pub-ch] (first connections)
          timeout-channel (timeout 1000)]
      (sub pub-ch message-id timeout-channel)
      (.send connection {:method method
                         :parameters parameters
                         :message-id message-id})
      ;; when reply is nil timeout exception
      (if-let [reply (<!! timeout-channel)]
        (do
          ;; if message contains :exception throw remote error
          (log/info "got answer" reply)
          (if-let [exception (:exception reply)]
            (let [exception-message (str (get-in reply [:exception :type]) ":"
                                         (get-in reply [:exception :message]))]
              (log/error "received remote error" exception-message)
              (throw (RemoteError. exception-message)))
            (:result reply)))
        (throw (Timeout. (str "Timeout of message " message-id))))))
  )


(defn client [service client-factory]
  (log/info "create client")
  (let [endpoints (discover service)
        connections (doall
                     (map #(list (client-factory service %1 %2) (pub %1 :message-id))
                          (repeatedly #(chan))
                          endpoints))]
    (->Client connections)))
