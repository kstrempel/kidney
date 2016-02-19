(ns kidney.transports.http-client
  (:refer-clojure :exclude [send read])
  (:require [kidney.interfaces :refer :all]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop <! >! close!]]
            [clojure.data.json :as json]
            [clj-http.client :as http]))

(deftype Connection [service received-ch endpoint]
    IConnection

    (send [this message]
      (go
        (let [message-buffer (json/write-str message)
              url (str "http://" endpoint "/" service)]
          (log/info "request to" url "with message" message-buffer)
          (try
            (let [request (http/post
                           url
                           {:body message-buffer})
                  body (:body request)]
              (log/info "got answer " request)
              (>! received-ch (json/read-str body :key-fn keyword)))
            (catch org.apache.http.NoHttpResponseException e
              (log/info "post request to" url "failed"))))))

  (close [this]
    (close! received-ch))

  (connect [this])

  (bind [this])

  (isAlive [this]
    ;; ping server
    true)
)

(defn client [service receive-ch endpoint]
 (->Connection service receive-ch endpoint))
