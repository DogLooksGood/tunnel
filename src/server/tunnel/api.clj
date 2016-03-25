(ns tunnel.api
  "API模块, 用于处理普通的HTTP请求."
  (:require [tunnel.service :as service]
            [taoensso.timbre :refer [debug spy]]
            [ring.util.response :refer [redirect]]))

;; =============================================================================
;; Helper

(defn gen-token
  []
  (.toString (java.util.UUID/randomUUID)))

;; =============================================================================
;; Protocol

(defmulti api-handler
  "处理HTTP的API请求
  key: 对应url中的 /api/:key
  params: 对应提交的参数"
  (fn [env key params] key))

;; =============================================================================
;; Implement

;; 登陆, 如果登陆成功, session中设置uid. redirect /
(defmethod api-handler :login
  [env _ params]
  (let [{:keys [username password]} params
        user (service/user-login username password)]
    (merge (redirect "/")
      {:session {:uid (:db/id user)}})))

;; 退出登陆, 清空session.
(defmethod api-handler :logout
  [env _ _]
  (let [{:keys [uid]} env]
    (service/user-logout uid)
    (debug "LOGOUT!!!!")
    {:session {}
     :body "success"}))

(defmethod api-handler :register
  [env _ params]
  (let [{:keys [username password]} params
        {status :status} (service/user-register username password)]
    (if (= status :success)
      (redirect "/login")
      (redirect "/register"))))

