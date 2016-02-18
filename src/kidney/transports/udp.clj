(ns kidney.transports.udp
  (:refer-clojure :exclude [send read])
  (:import (java.net InetAddress DatagramPacket DatagramSocket SocketException))
  (:require [kidney.interfaces :refer :all]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop close! <! >!]]
            [clojure.data.json :as json]))

(defn localhost [] (. InetAddress getLocalHost))

(defn- listen [ch socket transform]
  (go
    (try
      (loop []
        (let [datagram (DatagramPacket. (byte-array 1024) 1024)]
          (.receive socket datagram)
          (log/info "received something" (String. (.getData datagram)))
          ;; if transform is nil we are a server
          (let [message (String. (.getData datagram) 0 (.getLength datagram))]
            (>! ch (if (nil? transform)
                     {:origin datagram
                      :message message}
                     (transform message)))))
          (recur))
      (catch SocketException e
        (when-not (.isClosed socket)
          (log/info "exception" e))))))


(deftype Connection [received-ch send-ch socket]
  IConnection

  (send [this message]
    (let [message-buffer (json/write-str message)
          packet (DatagramPacket. (.getBytes message-buffer)
                                  (.length message-buffer)
                                  (localhost)
                                  8080)]
      (.send socket packet)))

  (disconnect [this]
    (.disconnect socket))

  (close [this]
    (close! received-ch)
    (when send-ch
      (close! send-ch))
    (.close socket))

  (connect [this]
    (listen received-ch socket #(json/read-str % :key-fn keyword)))

  (bind [this]
    ;; listen loop
    (listen received-ch socket nil)

    ;; reply loop
    (go-loop []
      (let [message (<! send-ch)
            payload (json/write-str (:message message))]
        (when message
          (log/info "Messsage to reply" message)
          (.send socket (DatagramPacket. (.getBytes payload)
                                         (.length payload)
                                         (.getSocketAddress (:origin
                                                              (:origin message)))))
          (recur)))))

  (isAlive [this]
    (.isAlive socket)))


(defn client [service received-ch endpoint]
  (let [socket (DatagramSocket. 8081)
        c (->Connection received-ch nil socket)]
    (.connect c)
    c))


(defn server [service received-ch send-ch endpoint]
  (let [socket (DatagramSocket. 8080)
        c (->Connection received-ch send-ch socket)]
    (.bind c)
    c))

