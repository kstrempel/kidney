(ns kidney.transports.http-client
  (:refer-clojure :exclude [send read])
  (:require [kidney.interfaces :refer :all]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop <! >!]]
            [clojure.data.json :as json]
            [clj-http.client :as http]))

(deftype Connection [service received-ch endpoint]
    IConnection

  (send [this message]
    (let [message-buffer (json/write-str message)
          url (str "http://" endpoint "/")]
      (log/info "request to" url "with message" message-buffer)
      (let [request (http/get
                     url)]
        (>! received-ch request))))

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
