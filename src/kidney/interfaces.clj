(ns kidney.interfaces
  (:refer-clojure :exclude [read send]))

(defprotocol IMessage
  (read [message])
  (write [message]))

(defprotocol IReceive
  (received [this message]))

(defprotocol IConnection
  (send [this message])
  (connect [this])
  (bind [this])
  (close [this])
  (isAlive [this]))

(defprotocol ICommunication
  )
