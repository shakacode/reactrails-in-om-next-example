(ns omnext-to-datomic.components
  (:require
   [sablono.core :as html :refer-macros [html]]
   [goog.dom :as gdom]
   [om.next :as om :refer-macros [defui]]))

(defn get-form-vals [e]
  (.preventDefault e)
  (let [author (.-value (gdom/getElement "author"))
        text (.-value (gdom/getElement "text"))]
    (aset  (gdom/getElement "text") "value" "") ;resets textaraa
    {:comment/author author :comment/text text}))

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
