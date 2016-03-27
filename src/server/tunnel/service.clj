(ns tunnel.service
  (:require [tunnel.db :as db]
            [taoensso.timbre :refer [trace error debug]]))

;; 这个模块需要重构!
;; TODO 需要统一返回格式, 这个模块似乎有点多余.

(defn user-register
  "用户注册"
  [username password]
  (try
    (db/mutate {} :user/register {:username username
                                  :password password})
    {:status :success}
    (catch Exception ex
      (trace ex)
      (error ex)
      {:status :error
       :message "注册失败"})))

(defn user-auto-login
  [uid]
  (debug "user-auto-login" uid)
  (db/mutate {} :user/set-status {:db/id uid :user/status :online}))

(defn user-login
  "用户登陆, 登陆成功返回用户信息, 登陆失败抛出异常."
  [username password]
  (if-let [user (db/query :user/login
                  '[:db/id :user/username :user/password]
                  {:user/username username
                   :user/password password})]
    user
    (throw
      (ex-info
        "Wrong username or password"
        {:type :user-not-found
         :cause :wrong-username-or-password}))))

(defn user-logout
  "用户登出"
  [uid]
  (when uid
    (db/mutate {} :user/set-status {:db/id uid
                                    :user/status :offline})))



