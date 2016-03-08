(ns tunnel.core
  (:require [tunnel.socket]             ; 初始化websocket
            [tunnel.style]              ; 加载样式
            [reagent.core :as r]
            [goog.dom :as gdom]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defonce app-state (atom {:text "Hello world!"}))

(defn hello-world
  []
  [:h1 "Hello, World!"])

(r/render-component
  [hello-world]
  (gdom/getElement "app"))

