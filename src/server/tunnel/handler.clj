(ns tunnel.handler
  (:require [tunnel.service :as service]
            [tunnel.subs :as subs]
            [tunnel.db :as db]
            [tunnel.api :as api]
            [tunnel.context :as ctx]
            [ring.util.response :refer [response redirect]]
            [taoensso.timbre :refer [debug spy error]]))

;; =============================================================================
;; Dynamic
(def ^:dynamic *session* nil)
(def ^:dynamic *cookie* nil)
(def ^:dynamic *request* nil)

;; =============================================================================
;; API Handler

(defn api-handler*
  "入口函数, 简易处理
  TODO 实现异常处理和跳转"
  [ring-req]
  (try
    (let [params* (:params ring-req)
          key (keyword (:key params*))
          params (dissoc params* :key)]
      (ctx/with-context ring-req
        (api/api-handler key params)))
    (catch clojure.lang.ExceptionInfo ex
      {:body (.getMessage ex)
       :headers {"Content-Type" "text/html"}
       :status 200})))

;; =============================================================================
;; Event Handler

;; 事件类型
;; :user/command         {:key :params}              发送命令
;; :user/fetch           {:key :selector :params}    查询数据
;; :user/register-sub    {:key :selector :params}    注册订阅
;; :user/unregister-sub  {:key :selector :params}    注销订阅

(defmulti event-msg-handler
  "处理WebSocket的事件"
  (fn [uid ev-id ev-msg]
    ev-id))

;; 这里的逻辑有一些混乱, 需要整理.
;; 所有的非常用的参数, 需要放到env中, 传递到query, mutate等函数中.
(defmethod event-msg-handler :user/command
  [uid ev-id [key params]]
  (when uid
    (debug "Handler user command: " key params)
    (let [env {:uid uid
               :session *session*}]
      {:status (db/mutate env key params)})))

(defmethod event-msg-handler :user/fetch
  [uid ev-id [key selector params :as expr]]
  {:value (db/query key selector params)
   :expr expr})

;; 先注册, 然后再查询.
;; TODO 这个实现应该有并发问题, 待解决
(defmethod event-msg-handler :user/register-sub
  [uid ev-id [key selector params :as expr]]
  (debug "fetch and register" key selector params)
  (subs/register-sub uid key selector params)
  {:value (db/query key selector params)
   :expr expr})

(defmethod event-msg-handler :user/unregister-sub
  [uid ev-id [key selector params]]
  (subs/unregister-sub uid key selector params)
  {})

;; 用户websocket断开时, 结束所有uid相关的订阅.
(defmethod event-msg-handler :chsk/uidport-close
  [uid ev-id _]
  (subs/unregister-all-subs uid)
  ;; 这里应该用户登出, 暂时不处理
  )

(defmethod event-msg-handler :default
  [uid ev-id ev-msg])

(defn event-msg-handler*
  "入口函数, 简易处理. 如果有?reply-fn就返回响应."
  [{:keys [event ring-req uid ?reply-fn]}]
  (let [[ev-id ev-msg] event]
    (ctx/with-context ring-req
      (let [data (event-msg-handler (ctx/current-uid) ev-id ev-msg)]
        (when ?reply-fn
          (?reply-fn data))))))
