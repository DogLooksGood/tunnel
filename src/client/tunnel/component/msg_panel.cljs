(ns tunnel.component.msg-panel
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [tunnel.markdown :refer [markdown]]
            [tunnel.remote :as remote]
            [tunnel.state :as state]))


(def ^:const ?msg-list [:message/list-all '[:db/id
                                            :message/content
                                            {:message/from [:user/username]}]
                        {}])


(defn msg-panel-render
  []
  (let [msg-list (state/remote ?msg-list)]
    [:div#msg-panel.msg-panel
     (for [msg (sort-by :db/id @msg-list)]
       ^{:key (:db/id msg)}
       [:div.msg
        [:div.msg-from
         (-> msg :message/from :user/username)]
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
     (fn []
       (remote/fetch-and-register ?msg-list))

     :component-did-update
     scroll-to-bottom
     
     :component-will-unmount
     (fn []
       (remote/unregister-sub ?msg-list))}))
