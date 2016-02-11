(ns kidney.transports.http-servlet
  (:import [javax.servlet.http HttpServletRequest HttpServletResponse])
  (:require [clojure.core.async :refer [<!! >!! chan]]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [clojure.string :a str]
            [clojure.string :as str]))

(gen-class :name kidney.transports.http-servlet.KidneyServlet
           :extends javax.servlet.http.HttpServlet)

(defonce servlet-channel-out (chan))
(defonce servlet-channel-in (chan))

(defn parse-body [request]
  (with-open [rdr (.getReader request)]
    (reduce str "" (line-seq rdr))))

(defn -doPost [this ^HttpServletRequest request ^HttpServletResponse response]
  (doto response
    (.setContentType "application/json")
    (.setStatus HttpServletResponse/SC_OK))
  (let [request-body (parse-body request)
        service (last (str/split (.getPathInfo request) #"/"))
        ch (chan)
        request-message {:service (keyword service)
                         :message request-body}]
    (>!! servlet-channel-out request-message)
    (.println (.getWriter response) (json/write-str (<!! servlet-channel-in)))))
