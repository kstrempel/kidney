(ns kidney.transports.http-server
  (:require [kidney.interfaces :refer :all]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop <! >!]]
            [clojure.data.json :as json]))

(def ^:dynamic server& (atom nil))

(defn- http-server [endpoint]
  (when (nil? @server&)
    (swap! server& into (.Server 8080)))
  @server&)

(deftype Connection [service receive-ch send-ch]
  IConnection

  (send [this message])

  (disconnect [this])

  (close [this])

  (connect [this])

  (bind [this])

  (isAlive [this]
    ;; ping server
    true)
  )

(defn server [service receive-ch send-ch endpoint]
  (->Connection service receive-ch send-ch))

(defn start-http [endpoint])

(defn stop-http [])
