(ns omnext-to-datomic.parser
  (:require [datomic.api :as d]))

;; =============================================================================
;; Reads
(defn comments
  ([db]
   (comments db nil))
  ([db selector]
   (comments db selector nil))
  ([db selector _]
   ;; TODO: Code to do as-of filtering is not complete and does not currently work.
   ;; This should be removed or made working.
   ;; Not using 3rd arg of params right now.
   (d/q
    '[:find [(pull ?eid selector) ...]
      :in $ selector
      :where
      [?eid :comment/author]]
    db (or selector '[*]))))

(defmulti readf (fn [env k params] k))

(defmethod readf :default
  [_ k _]
  (println "No handler for read key " k)
  {:value {:error (str "No handler for read key " k)}})

(defmethod readf :comment/list
  [{:keys [conn query] :as req} _ params]
  {:value (comments (d/db conn) query params)})


;; =============================================================================
;; Mutations

(defmulti mutatef (fn [env k params] k))

(defmethod mutatef :default
  [_ k _]
  {:value {:error (str "No handler for mutation key " k)}})

(defmethod mutatef 'comment/create
  [{:keys [conn]} k {:keys [:comment/author :comment/text :comment/time] :as com}]
  {:value {:keys  [:comments/list]}
   :action
   (fn []
     @(d/transact conn
        [{:db/id          #db/id[:db.part/user]
          :comment/author author
          :comment/text   text
          :comment/time   time}])
     200)})

