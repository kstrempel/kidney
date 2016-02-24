(ns kidney.discovery.zookeeper
  (:import (java.util UUID))
  (:require [zookeeper :as zk]
            [zookeeper.data :as data]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]))

(def main-path "/kidney/services")

(declare service-watcher)

(def zkconnection (zk/connect "127.0.0.1:2181"))

(defn service-watcher [event]
  (log/info "watcher " event)
  (zk/children zkconnection main-path :watcher service-watcher))

(defn zkclient []
  (let [client zkconnection]
    (when-not (zk/exists client main-path)
      (zk/create-all main-path))
    (zk/children client main-path)
    client))

(defn register [client service data]
  (let [uuid (str (UUID/randomUUID))
        path (str main-path "/" service "/" uuid)]
    (zk/create-all client path :watcher service-watcher)
    (zk/set-data client path
                 (data/to-bytes (json/json-str data))
                 0)
    (log/info "register" service ":" data)
    uuid))

(defn unregister [client service id]
  (zk/delete client (str main-path "/" service "/" id))
  (log/info "unregister" service ":" id))
