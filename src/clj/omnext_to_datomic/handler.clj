(ns omnext-to-datomic.handler
  (:gen-class)
  (:import (java.io ByteArrayOutputStream))
  (:require [cognitect.transit :as transit]
            [cognitect.transit :as t]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as rr]
            [ring.middleware.transit :as tr]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [omnext-to-datomic.db :as db]))

(defn write-transit [x]
  (let [baos (ByteArrayOutputStream.)
        w    (t/writer baos :json)
        _    (t/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(defn transit-response [response]
  {:status 200
   :headers {"Content-Type" "application/transit+json; charset=utf-8"}
   :body (write-transit response)})

(defn get-comments []
  (transit-response
   { :comment/list  (db/comments)}))

(defn save-comment [comment]
 (db/add-comment comment)
 (get-comments))

(defn process-query [{:keys [transit-params] :as req}]
  ; this is very naive and should be replaced with the om.next parser
  (if  (and (vector? transit-params)
            (= (ffirst transit-params) `submit/comment))
    (save-comment (-> transit-params first second))
    (get-comments)))

(defroutes app-routes 
  (GET "/" [] (rr/content-type (rr/resource-response "index.html" {:root "public"}) "text/html"))
  (POST "/api" [] process-query )
  (route/not-found "Not Found"))

(defn wrap-transit-logging [handler]
  (fn [req]
    (when-let [transit (:transit-params req)]
      (println "Logging transit-params" transit))
    (handler req)))

(def app
  (-> app-routes
     ; wrap-transit-logging
      wrap-content-type
      (wrap-resource "public")
      wrap-params
      tr/wrap-transit-params))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))
        handler (if (env :production) (wrap-reload app) app)]
    (run-server handler {:port port :join? false})
    (println "Server started on port " port)))
