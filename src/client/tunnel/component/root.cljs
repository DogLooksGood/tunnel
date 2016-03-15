(ns tunnel.component.root
  (:require [reagent.core :as r]
            [tunnel.component.menu :refer [menu]]
            [tunnel.component.input-panel :refer [input-panel]]
            [tunnel.component.msg-panel :refer [msg-panel]]))

(defn root
  []
  [:div.container
   [menu]
   [:div.content
    [:div.header
     [:span "A Chatroom written in pure Clojure ^_^"]]
    [input-panel]
    [msg-panel]]])
