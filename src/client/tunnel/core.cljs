(ns tunnel.core
  (:require [tunnel.socket :as socket]  ; 初始化websocket
            [tunnel.style.screen]       ; 加载样式
            [tunnel.remote :as remote]
            [tunnel.state :as state]
            [tunnel.component.root :refer [root]]
            [reagent.core :as r]
            [goog.dom :as gdom]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defonce app-state (atom {:text "Hello world!"}))

(def user-expr [:user/list-all '[*] {}])

(def hello-world
  (r/create-class
    {:reagent-render
     (fn [{:keys [i]}]
       (let [user-list (state/remote user-expr)]
         [:div
          [:button {:on-click state/log-state}
           "LOG"]
          (for [user @user-list]
            ^{:key (:db/id user)} [:div (:user/username user)])
          [:button {:on-click #(remote/fetch user-expr)}
           "FETCH"]
          i]))
     :component-will-mount
     (fn [this]
       ;; (remote/fetch-and-register user-expr)
       )
     :component-will-unmount
     (fn [this]
       ;; (remote/unregister-remote-sub user-expr)
       )}))

(r/render-component
  [root]
  (gdom/getElement "app"))

