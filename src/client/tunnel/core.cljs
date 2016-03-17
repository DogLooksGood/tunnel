(ns tunnel.core
  (:require [tunnel.socket :as socket]  ; 初始化websocket
            [tunnel.style.screen]       ; 加载样式
            [tunnel.remote :as remote]
            [tunnel.state :as state]
            [tunnel.register]
            [tunnel.component.root :refer [root]]
            [tunnel.protocol :refer [register-command]]
            [schema.core :as s :include-macros true]
            [reagent.core :as r]
            [goog.dom :as gdom]))

(enable-console-print!)

;; (def hello-world
;;   (r/create-class
;;     {:reagent-render
;;      (fn [{:keys [i]}]
;;        (let [user-list (state/remote user-expr)]
;;          [:div
;;           [:button {:on-click state/log-state}
;;            "LOG"]
;;           [:button {:on-click #(remote/fetch user-expr)}
;;            "FETCH"]
;;           [:button {:on-click #(remote/dispatch [:user/send-message {:content "hello"}])}
;;            "DISPATCH"]
;;           i]))
;;      :component-will-mount
;;      (fn [this]
;;        ;; (remote/fetch-and-register user-expr)
;;        )
;;      :component-will-unmount
;;      (fn [this]
;;        ;; (remote/unregister-remote-sub user-expr)
;;        )}))

(r/render-component
  [root]
  (gdom/getElement "app"))

