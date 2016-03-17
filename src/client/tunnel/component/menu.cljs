(ns tunnel.component.menu
  (:require [reagent.core :as r]
            [tunnel.protocol :as p]
            [tunnel.remote :as remote]
            [tunnel.state :as state]))

(def user-list-q
  [:user/list-all {}])

(def menu
  (r/create-class
    {:reagent-render
     (fn []
       (let [user-list (state/remote user-list-q)
             current-user (state/remote [:user/who-am-i {}])]
         [:div.menu
          [:div.menu-profile
           [:div.username (:user/username @current-user)]]
          [:div.menu-list
           [:ul.list.menu-item-list
            [:li.item.menu-item-title "Users:"]
            (let [user-groups (group-by :user/status @user-list)
                  users (lazy-cat (sort-by :user/username (:online user-groups))
                          (sort-by :user/username (:offline user-groups)))]
              (for [user users]
                (if (= :online (:user/status user))
                  ^{:key (:db/id user)}
                  [:li.item.menu-item.online (:user/username user)]
                  ^{:key (:db/id user)}
                  [:li.item.menu-item (:user/username user)])))]]]))

     :component-will-mount
     (fn []
       (remote/fetch [:user/who-am-i {}])
       (remote/subscribe user-list-q)
       )

     :component-will-unmount
     (fn []
       (remote/unsubscribe user-list-q)
       )}))

