(ns tunnel.api
  "API模块, 用于处理普通的HTTP请求."
  (:require [tunnel.page :as page]
            [datomic.api :as d]
            [taoensso.timbre :refer [debug spy]]
            [ring.util.response :as resp :refer [redirect]]))

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

;; 用户登陆, 如果登陆成功, 跳转/
;; 如果登陆失败, 打印错误信息
(defmethod api-handler :login
  [{:keys [conn]} _ params]
  (let [{:keys [username password]} params]
    (if-let [e (d/q '[:find ?e .
                      :in $ ?username ?password
                      :where
                      [?e :user/username ?username]
                      [?e :user/password ?password]]
                 (d/db conn) username password)]
      (merge (redirect "/")
        {:session {:uid e}})
      (page/error-page nil "用户名或密码错误"))))

(defmethod api-handler :logout
  [{:keys [conn req]} _ params]
  (let [e (spy (-> req :session :uid))]
    ;; 清空session
    (spy (merge (redirect "/")
           {:session {}}))))

(defmethod api-handler :register
  ;; 这里有线程安全问题.
  [{:keys [conn]} _ {:keys [username password]}]
  @(d/transact conn
     [{:db/id #db/id [:db.part/user]
       :user/username username
       :user/password password
       :user/status :offline}])
  (redirect "/login"))

