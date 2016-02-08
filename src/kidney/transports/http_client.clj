(ns kidney.transports.http-client
  (:require [kidney.interfaces :refer :all]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop <! >!]]
            [clojure.data.json :as json]
            [clj-http.client :as http]))

(deftype Connection [service received-ch endpoint]
    IConnection

  (send [this message]
    (let [message-buffer (json/write-str message)]
      (future
        (let [request (http/post
                       (str "http://" endpoint "/" service)
                       {:body message-buffer})]))))

  (disconnect [this])

  (close [this])

  (connect [this])

  (bind [this])

  (isAlive [this]
    ;; ping server
    true)
  

)

(defn client [service receive-ch endpoint]
 (->Connection service receive-ch endpoint))
