(ns kidney.interfaces)

(defprotocol IMessage
  (read [message])
  (write [message]))

(defprotocol IReceive
  (received [this message]))

(defprotocol IConnection
  (send [this message])
  (connect [this])
  (disconnect [this])
  (bind [this])
  (close [this])
  (isAlive [this]))

(defprotocol ICommunication
  )
