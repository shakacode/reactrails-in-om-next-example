(ns omnext-to-datomic.db
  (:require [datomic.api :refer [q db tempid] :as d]))

(def schema [{:db/id (tempid :db.part/db)
              :db/ident :comment/author
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}
             {:db/id (tempid :db.part/db)
              :db/ident :comment/text
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/id (tempid :db.part/db)
              :db/ident :comment/time
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

(def sample-comments [ {:comment/author "Bob"
                        :comment/time 1450142890738
                        :comment/text "The Price Is Right!"}])


(def uri  "datomic:mem://sample")

(defn install-schema [conn]
  @(d/transact conn schema))

(defn add-tempid [m]
  (assoc m :db/id (tempid :db.part/user)))

(defn add-comment [conn c]
  (let [tx (add-tempid c)]
    (d/transact conn [tx])))

(defn populate-db [conn]
  (doseq [comment sample-comments]
    (add-comment conn comment)))

(defn initialize-db []
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (install-schema conn)
    (populate-db conn)))

(initialize-db)
