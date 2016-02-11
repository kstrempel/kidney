(ns kidney.transports.http-server
  (:refer-clojure :exclude [send read])
  (:import [org.eclipse.jetty.server Server]
           [org.eclipse.jetty.servlet ServletHandler]
           [kidney.transports.http-servlet KidneyServlet])
  (:require [kidney.interfaces :refer :all]
            [kidney.transports.http-servlet :refer [servlet-channel-out
                                                    servlet-channel-in]]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop <! >! pub sub chan]]
            [clojure.data.json :as json]))

(def ^:dynamic server& (atom nil))

(deftype Connection [service receive-ch send-ch]
  IConnection

  (send [this message])

  (disconnect [this])

  (close [this])

  (connect [this])

  (bind [this]
    (go
      (let [ch (sub (pub servlet-channel-out :service)
                    (keyword service) (chan))]
        (loop []
          (let [message (<! ch)]
            (>! receive-ch message)
            (when-let [result (<! send-ch)]
              (>! servlet-channel-in (:message result))))
          (recur)))))

  (isAlive [this]
    ;; ping server
    true)
  )

(defn server [service receive-ch send-ch endpoint]
  (let [connection (->Connection service receive-ch send-ch)]
    (.bind connection)
    connection))

(defn start-http []
  (log/info "start http server")
  (let [server (Server. 8080)
        handler (ServletHandler.)]
    (.setHandler server handler)
    (.addServletWithMapping handler KidneyServlet "/*")
    (.start server)
    (reset! server& server)
    (Thread/sleep 1000)))

(defn stop-http []
  (log/info "stop http server")
  (.stop @server&)
  (.join @server&))
