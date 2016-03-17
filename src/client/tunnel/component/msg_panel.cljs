(ns tunnel.component.msg-panel
  (:require [goog.dom :as gdom]
            [goog.i18n.DateTimeFormat :as dtf]
            [reagent.core :as r]
            [tunnel.protocol :as p]
            [schema.core :as s]
            [tunnel.markdown :refer [markdown]]
            [tunnel.remote :as remote]
            [tunnel.state :as state]))

(p/register-query :message/list-all s/Any)


(def msg-list-q
  [:message/list-all
   {:sel '[:db/id
           :message/content
           :message/time
           {:message/from [:user/username]}]}])

(defn msg-panel-render
  []
  (let [msg-list (state/remote msg-list-q)]
    [:div#msg-panel.msg-panel
     (for [msg (sort-by :db/id @msg-list)]
       ^{:key (:db/id msg)}
       [:div.msg
        [:div.msg-from
         (-> msg :message/from :user/username)]
        [:div.msg-time
         (.format (goog.i18n.DateTimeFormat. "MM-dd HH:mm:ss")
           (:message/time msg))]
        [:div.msg-content
         {:dangerouslySetInnerHTML
          {:__html (markdown (:message/content msg))}}]])]))

(defn- scroll-to-bottom
  []
  (let [e (gdom/getElement "msg-panel")]
    (set! (.-scrollTop e) (.-scrollHeight e))))


(def msg-panel
  (r/create-class
    {:reagent-render
     msg-panel-render

     :component-will-mount
     (fn [this]
       (remote/subscribe msg-list-q))

     :component-did-update
     scroll-to-bottom
     
     :component-will-unmount
     (fn []
       (remote/unsubscribe msg-list-q))}))
