(ns kidney.transports.http-servlet
  (:import [javax.servlet.http HttpServletRequest HttpServletResponse])
  (:require [clojure.core.async :refer [<!! >!! chan]]))

(gen-class :name kidney.transports.http-servlet.KidneyServlet
           :extends javax.servlet.http.HttpServlet)

(defonce servlet-channel (chan))

(defn -doGet [this ^HttpServletRequest request ^HttpServletResponse response]
  (println "Hello Servlet")
  (doto response
    (.setContentType "text/html")
    (.setStatus HttpServletResponse/SC_OK))
  (.println (.getWriter response) "<h1>Hello Servlet<h1>"))
