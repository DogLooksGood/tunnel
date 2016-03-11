(ns tunnel.handler
  (:require [tunnel.service :as service]
            [tunnel.subs :as subs]
            [tunnel.db :as db]
            [ring.util.response :refer [response redirect]]
            [taoensso.timbre :refer [debug spy error]]))

;; =============================================================================
;; Dynamic
(def ^:dynamic *session* nil)
(def ^:dynamic *cookie* nil)
(def ^:dynamic *request* nil)

;; =============================================================================
;; Helpers

(defn gen-token
  []
  (.toString (java.util.UUID/randomUUID)))

;; =============================================================================
;; API Handler

(defmulti api-handler
  "处理HTTP的API请求"
  (fn [key params] key))

;; 用户登陆
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

(defn api-handler*
  "入口函数, 简易处理
  TODO 实现异常处理和跳转"
  [ring-req]
  (try
    (let [params* (:params ring-req)
          key (keyword (:key params*))
          params (dissoc params* :key)]
      (binding [*request* ring-req
                *session* (:session ring-req)
                *cookie* (:cookie ring-req)]
        (api-handler key params)))
    (catch clojure.lang.ExceptionInfo ex
      {:body (.getMessage ex)
       :headers {"Content-Type" "text/html"}
       :status 200})))

;; =============================================================================
;; Event Handler

;; 常用的事件类型
;; :user/command         {:key :params}              发送命令
;; :user/fetch           {:key :selector :params}    查询数据
;; :user/register-sub    {:key :selector :params}    注册订阅
;; :user/unregister-sub  {:key :selector :params}    注销订阅

(defmulti event-msg-handler
  "处理WebSocket的事件"
  (fn [uid ev-id ev-msg ?reply-fn]
    ev-id))

(defmethod event-msg-handler :user/command
  [uid ev-id ev-msg ?reply-fn])

(defmethod event-msg-handler :user/fetch
  [uid ev-id {:keys [key selector params]} ?reply-fn]
  {:value (db/query key selector params)})

(defmethod event-msg-handler :user/register-sub
  [uid ev-id {:keys [key selector params]} ?reply-fn]
  (subs/register-sub uid key selector params))

(defmethod event-msg-handler :user/unregister-sub
  [uid ev-id ev-msg ?reply-fn])

(defmethod event-msg-handler :chsk/uidport-close
  [uid _ _ _]
  (subs/unregister-all-subs uid))

(defmethod event-msg-handler :default
  [uid ev-id ev-msg ?reply-fn])

(defn event-msg-handler*
  "入口函数, 简易处理"
  [{:keys [event ring-req uid ?reply-fn]}]
  (binding [*request* ring-req]
    (let [reply (event-msg-handler uid (first event) (second event) ?reply-fn)]
      (when ?reply-fn
        ?reply-fn reply))))
