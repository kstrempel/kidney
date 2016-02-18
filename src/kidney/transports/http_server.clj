(ns kidney.transports.http-server
  (:refer-clojure :exclude [send read])
  (:import [org.eclipse.jetty.server Server]
           [org.eclipse.jetty.servlet ServletHandler ServletHolder]
           [org.eclipse.jetty.servlet ServletContextHandler]
           [javax.servlet.http HttpServletRequest HttpServletResponse]
           [javax.servlet.http HttpServlet])
  (:require [kidney.interfaces :refer :all]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop <!! >!! <! >! pub sub chan close!]]
            [clojure.string :as str]
            [clojure.data.json :as json]))

(def ^:dynamic server& (atom nil))
(def servlet-channel-out (atom nil))

(deftype Connection [service receive-ch send-ch]
  IConnection

  (send [this message])

  (disconnect [this])

  (close [this])

  (connect [this])

  (bind [this]
    (go
      (let [ch (sub (pub @servlet-channel-out :service)
                    (keyword service) (chan))]
        (loop []
          (when-let [message (<! ch)]
            (>! receive-ch message)
            (recur)))))

    (go-loop []
      (when-let [result (<! send-ch)]
        (log/info result)
        (let [message (:message result)
              actx (get-in result [:origin :origin ])
              response (.getResponse actx)]
          (doto response
            (.setContentType "application/json")
            (.setStatus HttpServletResponse/SC_OK))
          (.println (.getWriter response) (json/write-str message))
          (.complete actx)
          (recur))))
    )

  (isAlive [this]
    ;; ping server
    true)
  )


(defn server [service receive-ch send-ch endpoint]
  (let [connection (->Connection service receive-ch send-ch)]
    (.bind connection)
    connection))

(defn parse-body [request]
  (with-open [rdr (.getReader request)]
    (reduce str "" (line-seq rdr))))

(def servlet
  (proxy [HttpServlet] []
    (doPost [^HttpServletRequest request ^HttpServletResponse response]
      (let [actx (.startAsync request request response)
            runnable (proxy [Runnable] []
                       (run []
                         (let [request-body (parse-body request)
                               service (last (str/split (.getPathInfo request) #"/"))
                               ch (chan)
                               request-message {:service (keyword service)
                                                :origin actx
                                                :message request-body}]
                           (log/info "received message" request-body)
                           (>!! @servlet-channel-out request-message))))]
        (.start actx runnable)))))

(defn- servlet-holder []
  (doto (ServletHolder.)
    (.setServlet servlet)))

(defn- create-webapp []
  (doto (ServletContextHandler.)
    (.setContextPath "/")
    (.addServlet (servlet-holder) "/*")))

(defn start-http []
  (reset! servlet-channel-out (chan))
  (log/info "start http server")
  (let [server (Server. 9999)]
    (.setHandler server (create-webapp))
    (.start server)
    (reset! server& server)))

(defn stop-http []
  (log/info "stop http server")
  (.stop @server&)
  (.join @server&)
  (close! @servlet-channel-out))
