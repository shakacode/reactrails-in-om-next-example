(ns omnext-to-datomic.parser
  (:refer-clojure :exclude [read])
  (:require [datomic.api :as d]))

;; =============================================================================
;; Reads

(defmulti readf (fn [env k params] k))

(defmethod readf :default
  [_ k _]
  (println "No handler for read key " k)
  {:value {:error (str "No handler for read key " k)}})

(defn comments
  ([db]
   (comments db nil))
  ([db selector]
   (comments db selector nil))
  ([db selector {:keys [filter as-of]}]
   (let [db (cond-> db
              as-of (d/as-of as-of))]
     (d/q
      '[:find [(pull ?eid selector) ...]
        :in $ selector
        :where
        [?eid :comment/author]]
      db (or selector '[*])))))

(defmethod readf :comment/list
  [{:keys [conn query] :as req} _ params]
  (println ":comment/list")
  {:value (comments (d/db conn) query #_params)})




;; =============================================================================
;; Mutations

(defmulti mutatef (fn [env k params] k))

(defmethod mutatef :default
  [_ k _]
  {:value {:error (str "No handler for mutation key " k)}})

(defmethod mutatef 'comment/create
  [{:keys [conn]} k {:keys [:comment/author :comment/text :comment/time] :as com}]
  {:value [:comments/list]
   :action
   (fn []
     @(d/transact conn
        [{:db/id          #db/id[:db.part/user]
          :comment/author author
          :comment/text   text
          :comment/time   time}]))})

