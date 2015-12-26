(ns clj.omnext-to-datomic.handler-test
  (:require [omnext-to-datomic.handler :as handler]
            [clojure.test :refer [deftest is]:as t]
            [ring.mock.request :as mock]
            [datomic.api :as d]
            [omnext-to-datomic.db :as db]))


(def test-uri "datomic:mem://test" )

(def ^:dynamic conn)

(defn db-fixture [test-function]
  (db/initialize-db test-uri)
  (binding [conn (d/connect test-uri)]  
    (test-function)
    (d/delete-database test-uri)))

(t/use-fixtures :once db-fixture)



(deftest test-index
  (let [ret (handler/app (mock/request :get "/"))]
    (is (= 200 (:status ret)))
    (is (= "text/html" (get-in ret [:headers "Content-Type"])))))


(deftest test-api
  (let [ret (handler/app (mock/request :post "/api"))]
    (is (= 200 (:status ret)))
    (is (= "application/transit+json" (get-in ret [:headers "Content-Type"])))))

(deftest test-404
  (let [ret (handler/app (mock/request :get "/Im-sorry-I-have-the-wrong-address"))]
    (is (= 404 (:status ret)))
    (is (= "text/html; charset=utf-8" (get-in ret [:headers "Content-Type"])))))


(deftest test-get-comments
  (let [ret (handler/parser
             {:conn conn}
             [{:comment/list [:comment/author :comment/time :comment/text]}])]
    (is (= (-> ret :comment/list first :comment/author)
           "Bob"))))


(deftest test-add-comment
  (let [ret (handler/parser
             {:conn conn}
             '[(comment/create {:comment/author "Fred", :comment/text "It's a Beautiful Day in the neighborhood.", :comment/time 1451099053434}) {:comment/list [:comment/author :comment/time :comment/text]}]

             )]
    (is (= (count (:comment/list ret))
           2))))
