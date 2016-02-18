(defproject kidney "0.3.0-SNAPSHOT"
  :description "Microservice core framework"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [zookeeper-clj "0.9.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.374"]
                 [clj-http "2.0.1"]
                 [org.eclipse.jetty/jetty-servlet "9.2.14.v20151106"]
                 [clj-time "0.11.0"]]
  :aot [kidney.core.exceptions.timeout
        kidney.core.exceptions.remoteerror
        kidney.interfaces
        kidney.core.client]
)
