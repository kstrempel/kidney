(ns kidney.core-test
  (:require [clojure.test :refer :all]
            [kidney.transports.udp :refer [client-connect server-serve]]
            [kidney.core.client :as c]
            [kidney.core.server :as s]))

(deftest create-all-clients
  (testing "Check if the client is creating all clients"
    (let [c (c/client "first" client-connect)]
      (is (= (count (:endpoints c)) 1))
      (c/stop c))))

(deftest communicate
  (testing "Check communication between client and server"
    (let [add-method #(+ (get % "a") (get % "b"))
          s (s/server "first" server-serve {"add" add-method})
          c (c/client "first" client-connect)]
      (is (= (c/request c "add" {:a 1 :b 2}) "3"))
      (c/stop c)
      (s/stop s))))
