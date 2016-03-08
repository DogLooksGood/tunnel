(ns tunnel.service
  (:require [tunnel.db :as db]))

(defn user-login?
  "用户登陆, 登陆成功返回用户实体, 登陆失败抛出异常."
  [username password]
  (if-let [user (db/query :user-login
                  '[:db/id :user/username :user/password]
                  {:username username
                   :password password})]
    user
    (throw
      (ex-info
        "Wrong username or password"
        {:type :user-not-found
         :cause :wrong-username-or-password}))))
