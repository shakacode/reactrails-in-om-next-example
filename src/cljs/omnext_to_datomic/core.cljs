(ns omnext-to-datomic.core
  (:require
   [cognitect.transit :as transit]
   [goog.dom :as gdom]
   [om.next :as om :refer-macros [defui]]
   [ajax.core :refer [GET POST]]
   [omnext-to-datomic.components :as components]))

(enable-console-print!)


(def init-data {:form/author ""
                :nav/index 0
                :comment/list [] })


(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :comment/form
  [{:keys [state] :as env} key params]
  (let [{:keys [form/author nav/index]} @state]
    {:value {:form/author author :nav/index index} }))

(defn get-comments [state]
  (let [st @state]
    (into [] (map (fn [[_ v]] v) (get st :comment/by-time)))))

(defmethod read :comment/list
  [{:keys [state] :as env} _ {:keys [remote?]}]
  (let [st @state]
    (if-let [comments (get st :comment/list)]
      {:value (get-comments state) :remote true}
      {:remote true})))

(defmulti mutate om/dispatch)

(defmethod mutate 'comment/create
  [{:keys [state]} _  c]
  {:remote true
   :action
   (fn []
     (let [time (:comment/time c)]
       (swap! state assoc-in [:comment/by-time time]  c)
       (swap! state update-in [:comment/list] conj [:comment/by-time time] )))})

(defmethod mutate 'change/author
  [{:keys [state]} _ {:keys [author]}]
  {:action #(swap! state assoc :form/author author)})

(defmethod mutate 'change/nav
  [{:keys [state]} _ {:keys [index]}]
  {:action #(swap! state assoc :nav/index index)})


;; -----------------------------------------------------------------------------
;; Reconciler and Remote


(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn transit-post [url]
  (fn [{:keys [remote]} callback]
    (POST url {:params remote
               :handler callback
               :error-handler error-handler})))

(def reconciler
  (om/reconciler
   {:state  init-data
    :parser (om/parser {:read read :mutate mutate})
    :send (transit-post "/api")}))

(om/add-root! reconciler
              components/RootView (gdom/getElement "app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
