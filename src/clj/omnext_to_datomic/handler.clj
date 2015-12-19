(ns omnext-to-datomic.handler
  (:gen-class)
  (:import (java.io ByteArrayOutputStream))
  (:require; [cognitect.transit :as transit]
            [cognitect.transit :as t]
            [datomic.api :as d]
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
            [om.next.server :as om]
            [omnext-to-datomic.parser :as parser]
            [omnext-to-datomic.db :as db]))

(defn write-transit [x]
  (let [baos (ByteArrayOutputStream.)
        w    (t/writer baos :json)
        _    (t/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))


(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/transit+json"}
   :body    (write-transit data)})


(defn api [req]
  (generate-response
   ((om/parser {:read parser/readf :mutate parser/mutatef})
    {:conn (:datomic-connection req)} (:transit-params req))))


(defroutes app-routes 
  (GET "/" [] (rr/content-type (rr/resource-response "index.html" {:root "public"}) "text/html"))
  (POST "/api" [] api )
  (route/not-found "Not Found"))

(defn wrap-transit-logging [handler]
  (fn [req]
    (when-let [transit (:transit-params req)]
      (println "Logging transit-params" transit))
    (handler req)))

(defn wrap-connection [handler]
  (fn [req]
    (let [uri  "datomic:mem://sample"
          conn (d/connect uri) ]
      (handler (assoc req :datomic-connection conn)))))

(def app
  (-> app-routes
     ; wrap-transit-logging
      wrap-connection
      wrap-content-type
      (wrap-resource "public")
      wrap-params
      tr/wrap-transit-params))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))
        handler (if (env :production) app (wrap-reload app))]
    (run-server handler {:port port :join? false})
    (println "Server started on port " port)))
