(ns tunnel.core
  (:require [tunnel.socket :as socket]             ; 初始化websocket
            [tunnel.style.screen]                  ; 加载样式
            [reagent.core :as r]
            [goog.dom :as gdom]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defonce app-state (atom {:text "Hello world!"}))

(defn hello-world
  []
  [:div [:div "Hello, World!"]
   [:button {:on-click #(socket/send! [:user/fetch {:key :user/list-all
                                                    :selector '[*]
                                                    :params {}}])}
    "Test Fetch!"]
   [:button {:on-click #(socket/send! [:user/register-sub
                                       {:key :user/list-all
                                        :selector '[*]
                                        :params {}}])}
    "Test Sub"]])

(r/render-component
  [hello-world]
  (gdom/getElement "app"))

