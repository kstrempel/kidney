(ns kidney.core.server
  (:refer-clojure :exclude [send read])
  (:import [kidney.interfaces IConnection])
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer (chan close! go-loop go <! >!)]
            [clojure.data.json :as json]))


(defprotocol IServer
  (start [this])
  (stop [this])
  (receive [this]))


(defrecord Server [received-ch send-ch connection methods]
  IServer

  (start [this]
    (go-loop []
      (when-let [message-pure (<! received-ch)]
        (log/info "received message" message-pure)
        (let [message (json/read-str (:message message-pure))
              method (get methods (get message "method"))
              result {:message-id (get message "message-id")}]
          (go
            (>! send-ch
                {:origin message-pure
                 :message (try
                            (assoc result
                                   :result
                                   (method (get message "parameters")))
                            (catch Exception e
                              (assoc result
                                     :exception
                                     {:type (.getName (class e))
                                      :message (str (.getMessage ^Exception e))})))})))
        (recur))))

  (stop [this]
    (.close ^IConnection connection)
    (close! received-ch)
    (close! send-ch))

  (receive [this]))


(defn server [service server-factory methods]
  (log/info "create server")
  (let [received-ch (chan)
        send-ch (chan)
        s (->Server received-ch send-ch
                    ;; put received-ch and send-ch in a defrecord for java
                    (server-factory service received-ch send-ch "localhost:8080")
                    methods)]
    (start s)
    s))
