(ns tunnel.handler
  (:require [tunnel.service :as service]
            [ring.util.response :refer [response redirect]]
            [taoensso.timbre :refer [debug spy error]]))

;; =============================================================================
;; Helpers

(defn gen-token
  []
  (.toString (java.util.UUID/randomUUID)))

;; =============================================================================
;; API Handler

(defmulti api-handler
  "处理HTTP的API请求"
  (fn [key params ring-req]
    key))

;; 用户登陆
(defmethod api-handler :login
  [key params ring-req]
  (let [{:keys [username password]} params
        user (service/user-login? username password)]
    (merge (redirect "/")
      {:session {:uid (:db/id user)
                 :token (gen-token)}})))

(defn api-handler*
  "入口函数, 简易处理
  TODO 实现异常处理和跳转"
  [ring-req]
  (try
    (debug ring-req)
    (let [params* (:params ring-req)
          key (keyword (:key params*))
          params (dissoc params* :key)]
      (api-handler key params ring-req))
    (catch clojure.lang.ExceptionInfo ex
      {:body (.getMessage ex)
       :headers {"Content-Type" "text/html"}
       :status 200})))

;; =============================================================================
;; Event Handler

(defmulti event-msg-handler
  "处理WebSocket的事件"
  (fn [ev-id ev-msg ring-req ?reply-fn]
    ev-id))

(defmethod event-msg-handler :default
  [ev-id ev-msg _ _]
  #_(prn ev-id ev-msg))

(defn event-msg-handler*
  "入口函数, 简易处理"
  [{:keys [event ring-req ?reply-fn]}]
  (event-msg-handler (first event) (second event) ring-req ?reply-fn))
