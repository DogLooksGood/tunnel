(ns tunnel.core
  (:require [tunnel.socket :as socket]  ; 初始化websocket
            [tunnel.style.screen]       ; 加载样式
            [tunnel.subs :as subs]
            [reagent.core :as r]
            [goog.dom :as gdom]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defonce app-state (atom {:text "Hello world!"}))

;; (defn hello-world
;;   []
;;   [:div [:div "Hello, World!"]
;;    [:button {:on-click #(socket/send! [:user/fetch {:key :user/list-all
;;                                                     :selector '[*]
;;                                                     :params {}}])}
;;     "Test Fetch!"]
;;    [:button {:on-click #(socket/send! [:user/register-sub
;;                                        {:key :user/list-all
;;                                         :selector '[*]
;;                                         :params {}}])}
;;     "Test Sub"]])


(def hello-world
  (r/create-class
    {:reagent-render
     (fn [{:keys [i]}]
       [:div
        [:button {:on-click ;; (subs/register-remote-sub :user/list-all '[*] {})
                  #(socket/send! [:user/register-sub [:user/list-all '[*] {}]])}
         "Click me!"]
        i])
     :component-will-mount
     (fn [this]
       (prn "props" (r/props this))
       (subs/register-remote-sub :user/list-all '[*] {}))
     :component-will-unmount
     (fn [this]
       (prn (r/props this))
       (subs/unregister-remote-sub :user/list-all '[*] {}))}))

(r/render-component
  [hello-world {:i 10}]
  (gdom/getElement "app"))

