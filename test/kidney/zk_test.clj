(ns kidney.zk-test
  (:require  [clojure.test :refer :all]
             [kidney.discovery.zookeeper :as d]))

(deftest check-zookeeper-watch
  (testing "check zookeeper watch function"
    (let [client (d/zkclient)
          service-id (d/register client "first"
                                 {:endpoint "localhost:8080"})]
      (Thread/sleep 2000)
      (d/unregister client "first" service-id))))
