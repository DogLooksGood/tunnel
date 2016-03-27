(ns tunnel.mutate
  (:require [datomic.api :as d]
            [tunnel.parser :refer [mutate]]))

(defmethod mutate :user/client-register
  ;; 注册用户的客户端链接.
  ;; 同时将用户的在线状态设置为 :online.
  [{:keys [conn uid]} key {:keys [client-id]}]
  @(d/transact conn
     [[:db/add uid :user/client-id client-id]
      [:db/add uid :user/status :online]]))

(defmethod mutate :user/client-unregister
  ;; 注销用户的客户端链接.
  ;; 如果当前的client-id和传入的client-id一致, 将client-id取消, 并且设置状态到 :busy.
  ;; 否则不做任何处理.
  [{:keys [conn uid]} key {:keys [client-id]}]
  (let [old-client-id (d/q '[:find ?v .
                             :in $ ?e
                             :where [?e :user/status ?v]]
                        (d/db conn) uid)]
    (when (= old-client-id client-id)
      @(d/transact conn
        [[:db/retract uid :user/client-id client-id]
         [:db/add uid :user/status :busy]]))))

(defmethod mutate :message/send
  ;; 需要处理消息的接受者, 目前先简单的发送给所有人.
  ;; TODO 消息接受者
  [{:keys [uid tempid conn]} key {:keys [to content]}]
  (let [time (java.util.Date.)
        msg {:db/id #db/id [:db.part/user]
             :message/from uid
             :message/content content
             :message/time time}]
    @(d/transact conn
       [msg])
    {:status :processed
     :tempid tempid}))

(defmethod mutate :stack/publish
  ;; 发布一个问题
  [{:keys [uid tempid conn]} key {:keys [title content]}]
  (let [time (java.util.Date.)
        stack {:db/id #db/id [:db.part/user]
               :stack/title title
               :stack/content content
               :stack/publish-time time
               :stack/status :open
               :stack/agree []
               :stack/disagree []
               :stack/author uid}]
    @(d/transact conn
       [stack])
    {:status :processed
     :tempid tempid}))

