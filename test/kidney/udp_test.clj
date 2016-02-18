(ns kidney.udp-test
  (:import (kidney.core.exceptions Timeout RemoteError))
  (:require [clojure.test :refer :all]
            [kidney.transports.udp :refer [client server]]
            [kidney.core.client :as c]
            [kidney.core.server :as s]))

(deftest create-all-clients
  (testing "Check if the client is creating all clients"
    (let [c (c/client "first" client)]
      (try
        (is (= (count (:connections c)) 1))
        (finally
          (c/stop c))))))

(deftest communicate
  (testing "Check communication between client and server"
    (let [add-method #(+ (get % "a") (get % "b"))
          s (s/server "first" server {"add" add-method})
          c (c/client "first" client)]
      (try
        (is (= (c/request c "add" {:a 1 :b 2}) 3))
        (finally
          (c/stop c)
          (s/stop s))))))

(deftest communicate-timeout
  (testing "Check communication with timeout between client and server"
    (let [sleep-method #(Thread/sleep (get % "span"))
          s (s/server "first" server {"sleep" sleep-method})
          c (c/client "first" client)]
      (try
        (is (thrown? Timeout (c/request c "sleep" {:span 1000})))
        (finally
          (c/stop c)
          (s/stop s))))))

(deftest communicate-exception
  (testing "Check communication with exception between client and server"
    (let [div-method #(/ (get % "a") (get % "b"))
          s (s/server "first" server {"div" div-method})
          c (c/client "first" client)]
      (try
        (is (thrown? RemoteError (c/request c "div" {:a 1 :b 0})))
        (finally
          (c/stop c)
          (s/stop s))))))
