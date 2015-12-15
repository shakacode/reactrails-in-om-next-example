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
              :db.install/_attribute :db.part/db}
             ])

(def sample-comments [ {:comment/author "Bob"
                        :comment/time 1450142890738
                 :comment/text "The Price Is Right!"}])

(def uri  "datomic:mem://sample")

(d/create-database uri)

(def conn (d/connect uri))

(defn install-schema []
  @(d/transact conn schema))

(defn add-tempid [m]
  (assoc m :db/id (tempid :db.part/user)))

(defn add-comment [c]
  (let [tx (add-tempid c)]
    (d/transact conn [tx])))

(defn populate-db []
  (doseq [comment sample-comments]
    (add-comment comment)))

(install-schema)

(populate-db)

(defn touch-ent [db id]
  (let [ent (d/entity db id)] 
    (if (nil? ent)
      nil
      (into {} (d/touch ent)))))

(defn comments [] 
  (let [this-db (db conn)
        result (q '[:find ?e
                    :where [?e :comment/author]]
                  this-db)]
    (map (fn [[id]] (touch-ent this-db id)) result)))
