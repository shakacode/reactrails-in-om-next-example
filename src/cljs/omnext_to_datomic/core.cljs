(ns omnext-to-datomic.core
  (:require
   [cognitect.transit :as transit]
   [goog.dom :as gdom]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :as html :refer-macros [html]]
   [ajax.core :refer [GET POST]]))

(enable-console-print!)


(def init-data {:form/author ""
                :nav/index 0
                :comment/list [] })

;; -----------------------------------------------------------------------------
;; Ajax


(defn merge-response [state res]
  (println res)
  (let [comments (:comment/list res)
        by-time  (into {}
                       (map (fn [c][(:comment/time c) c]) comments))
        comments (map (fn [c] [:comment/by-time (:comment/time c)]) comments)]
    (-> state
        (update :comment/by-time merge by-time)
        (update :comment/list concat comments))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-form-vals [e]
  (.preventDefault e)
  (let [author (.-value (gdom/getElement "author"))
        text (.-value (.getElementById js/document "text"))]
    (aset  (.getElementById js/document "text") "value" "") ;resets textaraa
    {:comment/author author :comment/text text}))

(defn transit-post [url]
  (fn [{:keys [remote]} callback]
    (POST url {:params remote
               :handler callback
               :error-handler error-handler})))


;; -----------------------------------------------------------------------------
;; Parsing


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
;; Components


(defui FormHorizontal
  static om/IQuery
  (query [this]
         [:form/author])
  Object
  (render [this]
          (let [{:keys [form/author] :as props} (om/props this)
                {:keys [submit change]} (om/get-computed this)]
            (html
             [:form {:class "commentForm form-horizontal"
                     :onSubmit submit}
              [:div {:class "form-group"}
               [:label {:class "control-label col-sm-2"} "Name"]
               [:div {:class "col-sm-10"}
                [:input {:type "text" :id "author" :name "author" :placeholder "Your Name"
                         :value author :class "form-control"
                         :onChange change}]]]
              [:div {:class "form-group"}
               [:label {:class "control-label col-sm-2"} "Text"]
               [:div {:class "col-sm-10"}
                [:input {:type "textarea" :id "text" :placeholder "Say something using Markdown"
                         :class "form-control" :name "text"}]]]
              [:div {:class "form-group"}
               [:div {:class "col-sm-offset-2 col-sm-10"}
                [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]]]))))


(defui FormStacked
  static om/IQuery
  (query [this]
         [:form/author])
  Object
  (render [this]
          (let [{:keys [form/author]} (om/props this)
                {:keys [change submit]} (om/get-computed this)]
            (html
             [:form {:class "commentForm "
                     :onSubmit submit}
              [:div {:class "form-group"}
               [:label {:class "control-label "} "Name"]
               [:input {:type "text" :id "author" :name "author" :placeholder "Your Name"
                        :value author :class "form-control"
                        :onChange change}]]
              [:div {:class "form-group"}
               [:label {:class "control-label "} "Text"]
               [:input {:type "textarea" :id "text" :placeholder "Say something using Markdown"
                        :class "form-control" :name "text"}]]
              [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]))))

(defui FormInline
  static om/IQuery
  (query [this]
         [:form/author])
  Object
  (render [this]
          (let [{:keys [form/author]} (om/props this)
                {:keys [submit change]} (om/get-computed this)]
            (html
             [:form {:class "commentForm" :onSubmit submit}
              [:div {:class "form-group"}
               [:label {:class "control-label "} "Inline Form"]
               [:div {:class "wrapper"}
                [:div {:class "row"}
                 [:div {:class "col-xs-3"}
                  [:input {:type "text" :id "author" :name "author" :placeholder "Your Name"
                           :value author :class "form-control"
                           :onChange change}]]
                 [:div {:class "col-xs-8"}
                  [:input {:type "textarea" :id "text" :placeholder "Say something using Markdown"
                           :class "form-control" :name "text"}]]
                 [:div {:class "col-xs-1"}
                  [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]]]]]))))

(def form-horizontal (om/factory FormHorizontal))
(def form-stacked (om/factory FormStacked))
(def form-inline (om/factory FormInline))

(defn add-time [c]
  (assoc c :comment/time (.now js/Date)))

(defui CommentForm
  static om/IQuery
  (query [this]
         [:nav/index :form/author])
  Object
  (render [this]
          (let [{:keys [nav/index] :as props} (om/props this)
                submit-fn (fn [e] (let [com (add-time
                                             (get-form-vals e))]
                                    (om/transact! this `[(comment/create ~com) :comment/list ])))
                change-fn (fn [e] (let  [a (-> e .-target .-value)]
                                    (om/transact! this `[(change/author {:author ~a})])))
                props (om/computed props {:submit submit-fn :change change-fn})]
            (condp = index
              0 (form-horizontal props)
              1 (form-stacked props)
              2 (form-inline props)))))

(def comment-form (om/factory CommentForm))

(defui FormSelector
  static om/IQuery
  (query [this]
         [:nav/index])
  Object
  (render [this]
          (let [{:keys [nav/index] :as props} (om/props this) ]
            (html
             [:nav
              [:ul {:class "nav nav-pills"}
               [:li (if (= 0 index) {:class "active"})
                [:a {:onClick #(om/transact! this '[(change/nav  {:index 0}) :nav/index])}
                 "Horizontal Form"]]
               [:li (if (= 1 index) {:class "active"})
                [:a {:onClick #(om/transact! this '[(change/nav  {:index 1}) :nav/index])}
                 "Stacked Form"]]
               [:li (if (= 2 index) {:class "active"})
                [:a {:onClick  #(om/transact! this '[(change/nav  {:index 2}) :nav/index])}
                 "Inline Form"]]]]))))

(def form-selector (om/factory FormSelector))

(defui CommentView
  static om/Ident
  (ident [this {:keys [comment/time]}]
         [:comment/by-time time])
  static om/IQuery
  (query [this]
         '[:comment/author :comment/time :comment/text])
  Object
  (render [this]
          (let [{:keys [comment/author comment/text comment/time]} (om/props this)]
            (html
             [:div {:class "comment" }
              [:h2 {:class "comment-author"} author]
              [:span {:class "comment-text"
                      :dangerouslySetInnerHTML
                      {:__html (-> text str js/marked)}}]]))))

(def comment-view (om/factory CommentView {:keyfn :comment/time}))

(defui CommentListView
  Object
  (render [this]
          (let [comments (sort-by :comment/time >  (om/props this))]
            (html
             [:ul nil
              (map comment-view comments)]))))

(def comment-list-view (om/factory CommentListView))

(defui RootView
  static om/IQuery
  (query [this]
         [{:nav/index (om/get-query FormSelector)}
          {:comment/list (om/get-query CommentView)}
          {:comment/form (om/get-query CommentForm)}])
  Object
  (render [this]
          (let [{:keys [nav/index comment/list comment/form] :as props} (om/props this)]
            (html
             [:div
              [:h1 "A Demonstration of a Clojure(Script) system from Om.next to Datomic"]
              [:p "Hot-reloading brought to you by Figwheel"]
              [:div {:class "comment-box"}
               [:h1 "Comments"]
               [:p "Text takes Github Flavored Markdown. Only Comments from the last 24 hours are displayed."
                [:br][:b "Name"] " is preserved." [:b "Text"] " is reset, between submits."]
               [:div {:class "commentBox container"}
                (form-selector props)
                [:hr]
                (comment-form form)
                (comment-list-view list)]]]))))

(def reconciler
  (om/reconciler
   {:state  init-data
 ;   :merge-tree merge-response
    :parser (om/parser {:read read :mutate mutate})
    :send (transit-post "/api")}))

(om/add-root! reconciler
              RootView (gdom/getElement "app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
