(ns tunnel.service
  (:require [tunnel.db :as db]))

(defn user-login
  "用户登陆, 登陆成功返回用户实体, 登陆失败抛出异常.
  如果登陆成功, 用户的状态会修改为在线 :online"
  [username password]
  (if-let [user (db/query :user/login
                  '[:db/id :user/username :user/password]
                  {:user/username username
                   :user/password password})]
    (do
      (db/mutate :user/set-status (merge user
                                    {:user/status :online}))
      user)
    (throw
      (ex-info
        "Wrong username or password"
        {:type :user-not-found
         :cause :wrong-username-or-password}))))



