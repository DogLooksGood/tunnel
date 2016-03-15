(ns tunnel.api
  (:require [tunnel.service :as service]
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
  (fn [key params] key))

;; =============================================================================
;; Implement

(defmethod api-handler :login
  [_ params]
  (let [{:keys [username password]} params
        user (service/user-login username password)]
    ;; 如果登陆成功, 给session中添加uid, 作为sente的标识.
    ;; TODO 随机生成的token, 目前没有用. 先放着.
    ;; 之后redirect到 "/" 
    (merge (redirect "/")
      {:session {:uid (:db/id user)
                 :token (gen-token)}})))

(defmethod api-handler :register
  [_ params]
  (let [{:keys [username password]} params
        {status :status} (service/user-register username password)]
    (if (= status :success)
      (redirect "/login")
      (redirect "/register"))))

