(ns tunnel.core
  (:require [tunnel.socket :as socket]  ; 初始化websocket
            [tunnel.style.screen]       ; 加载样式
            [tunnel.client-utils :as u]
            [tunnel.remote :as remote]
            [tunnel.state :as state]
            [tunnel.register]
            [tunnel.component.root :refer [root]]
            [tunnel.protocol :refer [register-command]]
            [schema.core :as s :include-macros true]
            [reagent.core :as r]
            [goog.dom :as gdom]))

(enable-console-print!)

(r/render-component
  [root]
  (gdom/getElement "app"))

