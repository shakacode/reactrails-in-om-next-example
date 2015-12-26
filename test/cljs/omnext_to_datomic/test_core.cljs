(ns omnext-to-datomic.test-core
  (:require [omnext-to-datomic.core :as core]
            [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [om.next :as om :refer-macros [defui]]))

(enable-console-print!)


(def init-data {:form/author "Joe Jackson"
                :nav/index 0
                :comment/list [] })


(defn setup []
  
  (println "setup"))

(defn teardown []
  (println "teardown"))

(defn each-fixture [f]
  (setup)
  (f)
  (teardown))

(use-fixtures :each each-fixture)

(deftest testing parser []
  (let [state  (atom (om/tree->db People init-data true))]
    (is (= "fromt" (core/parser {:state state} :form/author)))
    ))
