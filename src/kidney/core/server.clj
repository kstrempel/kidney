(ns kidney.core.server
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer (chan close! go-loop go <! >!)]
            [clojure.data.json :as json]))


(defprotocol IServer
  (start [this])
  (stop [this])
  (receive [this]))


(defrecord Server [receive-ch send-ch connection methods]
  IServer

  (start [this]
    (go-loop []
      (when-let [message-pure (<! receive-ch)]
        (log/info "received message from async" message-pure)
        (let [message (json/read-str (:message message-pure))
              method (get methods (get message "method"))]
          (>! send-ch {:origin (:origin message-pure)
                       :message {:result (method (get message "parameters"))
                                 :message-id (get message "message-id")}}))
        (recur))))

  (stop [this]
    (.close connection)
    (close! receive-ch)
    (close! send-ch))

  (receive [this]))


(defn server [service server-factory methods]
  (log/info "create server")
  (let [receive-ch (chan)
        send-ch (chan)
        s (->Server receive-ch send-ch
                    ;; put receive-ch and send-ch in a defrecord for java
                    (server-factory receive-ch send-ch "localhost:8080")
                    methods)]
    (start s)
    s))
