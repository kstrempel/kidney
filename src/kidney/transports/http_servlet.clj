(ns kidney.transports.http-servlet
  (:import [javax.servlet.http HttpServletRequest HttpServletResponse])
  (:require [clojure.core.async :refer [<!! >!! chan]]
            [clojure.tools.logging :as log]))

(gen-class :name kidney.transports.http-servlet.KidneyServlet
           :extends javax.servlet.http.HttpServlet)

(defonce servlet-channel (chan))

(defn -doPost [this ^HttpServletRequest request ^HttpServletResponse response]
  (log/info "get request")
  (doto response
    (.setContentType "application/json")
    (.setStatus HttpServletResponse/SC_OK))
  (.println (.getWriter response) "5"))

(defn -doGet [this ^HttpServletRequest request ^HttpServletResponse response]
  (log/info "get request")
  (doto response
;;    (.setContentType "application/json")
    (.setStatus HttpServletResponse/SC_OK))
  (.println (.getWriter response) "5"))
