(ns tunnel.mutate
  (:require [datomic.api :as d]
            [tunnel.parser :refer [mutate]]))

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

