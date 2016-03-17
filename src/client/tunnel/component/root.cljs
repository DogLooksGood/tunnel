(ns tunnel.component.root
  (:require [reagent.core :as r]
            [tunnel.remote :as remote]
            [tunnel.component.menu :refer [menu]]
            [tunnel.component.input-panel :refer [input-panel]]
            [tunnel.component.msg-panel :refer [msg-panel]]))

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
           [:i.fa.fa-sign-out.btn]]]
         [input-panel]
         [msg-panel]]])}))


