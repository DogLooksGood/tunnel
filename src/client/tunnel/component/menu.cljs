(ns tunnel.component.menu
  (:require [reagent.core :as r]
            [tunnel.remote :as remote]
            [tunnel.state :as state]))

(def ^:const ?user-list [:user/list-all '[*] {}])

(def menu
  (r/create-class
    {:reagent-render
     (fn []
       (let [user-list (state/remote ?user-list)]
         [:div.menu
          [:div.menu-profile
           [:div.username "TUNNEL"]]
          [:div.menu-list
           [:ul.list.menu-item-list
            [:li.item.menu-item-title "所有用户"]
            (for [user (sort-by :user/username @user-list)]
              ^{:key (:db/id user)}
              [:li.item.menu-item (:user/username user)])]]]))

     :component-will-mount
     (fn []
       (remote/fetch-and-register ?user-list))

     :component-will-unmount
     (fn []
       (remote/unregister-sub ?user-list))}))
