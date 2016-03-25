(ns tunnel.handler
  (:require [tunnel.service :as service]
            [tunnel.subs :as subs]
            [tunnel.db :as db]
            [tunnel.api :as api]
            [tunnel.parser :as parser]
            [tunnel.context :as ctx]
            [ring.util.response :refer [response redirect]]
            [reloaded.repl :refer [system]]
            [taoensso.timbre :refer [debug spy error]]))

;; =============================================================================
;; API Handler

(defn api-handler*
  "入口函数, 简易处理
  TODO 实现异常处理和跳转"
  [ring-req]
  (try
    (let [params* (:params ring-req)
          key (keyword (:key params*))
          params (dissoc params* :key)
          env {:ring-req ring-req
               :uid (-> ring-req :session :uid)}]
      (ctx/with-context ring-req
        (debug "API Access:" key params)
        (api/api-handler env key params)))
    (catch clojure.lang.ExceptionInfo ex
      {:body (.getMessage ex)
       :headers {"Content-Type" "text/html"}
       :status 200})))

;; =============================================================================
;; Event Handler

(defmulti event-msg-handler
  "处理WebSocket的事件"
  (fn [msg ev-id ev-msg]
    ev-id))

(defmethod event-msg-handler :user/command
  [{:keys [ring-req uid]} ev-id {:keys [tempid key params]}]
  (db/with-conn conn
    (let [send! (-> system :sente :chsk-send!)
          env {:req ring-req
               :uid uid
               :tempid tempid
               :conn conn
               :send! send!}]
      (parser/mutate env key params))))

(defmethod event-msg-handler :user/fetch
  [{:keys [ring-req uid]} ev-id [key params :as ev-msg]]
  (debug "user/fetch:" ev-msg)
  (db/with-conn-db conn db
    (let [env {:req ring-req
               :uid uid
               :db db
               :conn conn}]
      {:value (parser/query env key params)
       :expr ev-msg})))

(defmethod event-msg-handler :user/subscribe
  [{:keys [ring-req uid] :as msg} ev-id [key params :as ev-msg]]
  (debug "user/subscribe:" ev-msg)
  (subs/register-sub uid key params)
  (event-msg-handler msg :user/fetch ev-msg))

(defmethod event-msg-handler :user/unsubscribe
  [{:keys [uid]} ev-id [key params :as ev-msg]]
  (subs/unregister-sub uid key params))

;; WebSocket断开连接.
(defmethod event-msg-handler :chsk/uidport-close
  [{:keys [ring-req uid client-id]} _ _]
  (subs/unregister-all-subs uid)
  (db/with-conn conn
    (let [env {:req ring-req
               :uid uid
               :conn conn}]
      (parser/mutate env :user/client-unregister {:client-id client-id})))
  (service/user-logout uid))

;; WebSocket连接成功.
(defmethod event-msg-handler :chsk/uidport-open
  [{:keys [ring-req uid client-id]} _ _]
  (db/with-conn conn
    (let [env {:req ring-req
               :uid uid
               :conn conn}]
      (parser/mutate env :user/client-register {:client-id client-id}))))

(defmethod event-msg-handler :default
  [msg ev-id ev-msg])

(defn event-msg-handler*
  "入口函数, 简易处理. 如果有?reply-fn就返回响应."
  [{:keys [event ring-req uid ?reply-fn] :as msg}]
  (let [[ev-id ev-msg] event]
    (let [data (event-msg-handler msg ev-id ev-msg)]
      (when ?reply-fn
        (?reply-fn data)))))
