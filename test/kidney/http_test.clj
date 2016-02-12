(ns kidney.http-test
  (:import (kidney.core.exceptions Timeout RemoteError))
  (:require [clojure.test :refer :all]
            [kidney.transports.http-server :refer [server start-http stop-http]]
            [kidney.transports.http-client :refer [client]]
            [kidney.core.client :as c]
            [kidney.core.server :as s]))

(deftest create-all-clients
  (testing "Check if the client is creating all clients"
    (start-http)
    (let [c (c/client "first" client)]
      (is (= (count (:connections c)) 1))
      (stop-http)
      (c/stop c))))

(deftest communicate
  (testing "Check communication between client and server"
    (start-http)
    (let [add-method #(+ (get % "a") (get % "b"))
          s (s/server "first" server {"add" add-method})
          c (c/client "first" client)]
      (is (= (c/request c "add" {:a 1 :b 2}) 3))
      (stop-http)
      (c/stop c)
      (s/stop s))))

(deftest communicate-timeout
  (testing "Check communication with timeout between client and server"
    (start-http)
    (let [sleep-method #(Thread/sleep (get % "span"))
          s (s/server "first" server {"sleep" sleep-method})
          c (c/client "first" client)]
      (is (thrown? Timeout (c/request c "sleep" {:span 1000})))
      (stop-http)
      (c/stop c)
      (s/stop s))))

(deftest communicate-exception
  (testing "Check communication with exception between client and server"
    (start-http)
    (let [div-method #(/ (get % "a") (get % "b"))
          s (s/server "first" server {"div" div-method})
          c (c/client "first" client)]
      (is (thrown? RemoteError (c/request c "div" {:a 1 :b 0})))
      (stop-http)
      (c/stop c)
      (s/stop s))))
