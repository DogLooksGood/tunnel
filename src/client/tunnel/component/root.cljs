(ns tunnel.component.root
  (:require [reagent.core :as r]
            [ajax.core :refer [POST]]
            [tunnel.remote :as remote]
            [tunnel.component.menu :refer [menu]]
            [tunnel.component.input-panel :refer [input-panel]]
            [tunnel.component.msg-panel :refer [msg-panel]]
            [goog.dom :as gdom])
  (:import [goog.net XhrIo]))

;; 直接跳转之后, 避免浏览器缓存, 追加一个等于当前时间的u参数.
(defn- logout
  []
  (let [token (.-value (gdom/getElement "__anti-forgery-token"))]
    (POST "/api/logout" {:headers {"X-CSRF-Token" token}
                         :handler (set! js/window.location
                                    (str "/login?u=" (.getTime (js/Date.))))})))

(def root
  (r/create-class
    {:reagent-render
     (fn
       []
       [:div.container
        [menu]
        [:div.content
         [:div.header
          [:span "A Chatroom written in pure Clojure ^_^"]
          [:span.icons.pull-right
           [:i.fa.fa-cogs.btn]
           [:i.fa.fa-sign-out.btn {:on-click logout}]]]
         [input-panel]
         [msg-panel]]])}))


