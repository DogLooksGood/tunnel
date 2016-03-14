(ns tunnel.db
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :refer [debug spy]]
            [datomic.api :as d]
            [tunnel.utils :as utils]))

(defmacro with-conn
  [conn & exprs]
  `(let [~conn (d/connect "datomic:mem://localhost:4334/test")]
     (do ~@exprs)))

;; 直接查询返回查询结果.
(defmulti query
  (fn [key selector params]
    key))

;; 传入tx-data限定entity范围的查询.
(defmulti scoped-query
  (fn [key selector params db tx-data]
    key))

(defmulti mutate
  (fn [env key params]
    key))

;; =============================================================================
;; Transaction Listener

(defn start-tx-listener
  "开始监听tx-report, 对每一个tx调用callback.
  只能启动一次.
  TODO reset的时候, loop里面的let那一行会抛InterruptedException.
  似乎不影响运行. 原因不明"
  [callback]
  (debug "Start TX listener.")
  (with-conn conn
    (let [tx-report (d/tx-report-queue conn)]
      (future
        (try
          (loop []
            (let [tx (.take tx-report)]
              (callback tx)
              (recur)))
          (catch InterruptedException ie))))))

(defn stop-tx-listener
  "停止监听tx-report."
  [tx-listener]
  (debug "Stop TX listener.")
  (future-cancel tx-listener))

(defrecord TxListener [callback tx-listener]
  component/Lifecycle
  (start [component]
    (assoc component
      :tx-listener (start-tx-listener callback)))
  (stop [component]
    (if-let [tx-listener (:tx-listener component)]
      (stop-tx-listener tx-listener)
      (dissoc component :tx-listener))))

(defn tx-listener
  [callback]
  (map->TxListener {:callback callback
                    :tx-listener nil}))

;; =============================================================================
;; DB Helper

(defn diff-result
  [key selector params tx]
  (let [db-before (:db-before tx)
        db-after (:db-after tx)
        tx-data (:tx-data tx)
        before (scoped-query key selector params db-before tx-data)
        after (scoped-query key selector params db-after tx-data)]
    (utils/diff :db/id before after)))

;; =============================================================================
;; Users


(defmethod query :user/login
  [key selector params]
  (let [{:keys [user/username user/password]} params]
    (with-conn conn
      (d/q '[:find (pull ?e selector) .
             :in $ selector ?u ?p
             :where
             [?e :user/username ?u]
             [?e :user/password ?p]]
        (d/db conn) selector username password))))

(defmethod query :user/list-all
  [key selector params]
  (with-conn conn
    (d/q '[:find [(pull ?e selector) ...]
           :in $ selector
           :where
           [?e :user/username]]
      (d/db conn) selector)))

(defmethod scoped-query :user/list-all
  [key selector params db tx-data]
  (with-conn conn
    (d/q '[:find [(pull ?e selector) ...]
           :in $ $tx-data selector
           :where
           [$tx-data ?e]
           [?e :user/username]]
      db tx-data selector)))

(defmethod query :message/list-all
  [key selector params]
  (with-conn conn
    (d/q '[:find [(pull ?e selector) ...]
           :in $ selector
           :where
           [?e :message/from]]
      (d/db conn) selector)))

(defmethod scoped-query :message/list-all
  [key selector params db tx-data]
  (with-conn conn
    (d/q '[:find [(pull ?e selector) ...]
           :in $ $tx-data selector
           :where
           [$tx-data ?e]
           [?e :message/from]]
      db tx-data selector)))

(defmethod mutate :user/register
  [env key params]
  (let [{:keys [username password]} params]
    (with-conn conn
      @(d/transact conn
         [{:db/id #db/id [:db.part/user]
           :user/username username
           :user/password password
           :user/status :offline}]))))

(defmethod mutate :user/set-status
  [env key params]
  (let [{:keys [user/status db/id]} params]
    (with-conn conn
      @(d/transact conn
         [[:db/add id :user/status :online]]))))

(defmethod mutate :user/add-tag
  [env key params]
  (let [{:keys [db/id user/tags]} params]
    (with-conn conn
      @(d/transact conn
         (mapv #(do [:db/add id :user/tags %]) tags)))))

(defmethod mutate :user/send-message
  ;; 需要处理消息的接受者, 目前先简单的发送给所有人.
  ;; TODO 消息接受者
  [{:keys [uid]} key {:keys [to content]}]
  (let [msg {:db/id #db/id [:db.part/user]
             :message/from uid
             :message/content content}]
    (with-conn conn
      @(d/transact conn
         [msg])
      ;; 随便返回点什么.
      :ok)))

;; =============================================================================
;; User friends

(defmethod query :user/friend-list
  [key selector params]
  (let [{:keys [db/id]} params]
    (with-conn conn
      (d/q '[:find [(pull ?f selector) ...]
             :in $ selector ?uid
             :where
             [?uid :user/friends ?f]]
        (d/db conn) selector id))))

(defmethod scoped-query :user/friend-list
  [key selector params db tx-data]
  (let [{:keys [db/id]} params]
    (d/q '[:find [(pull ?f selector) ...]
           :in $ $tx-data selector ?uid
           :where
           (or
             [$tx-data ?uid]
             [$tx-data ?f])
           [$ ?uid :user/friends ?f]]
      db tx-data selector id)))

;; test insert
(comment
  (with-conn conn
    @(d/transact conn
       [{:db/id #db/id [:db.part/user]
         :user/username "狗好看"
         :user/password "321"
         :user/status :offline}]))

  (clojure.pprint/pprint
    (with-conn conn
      (d/q '[:find [(pull ?e [:db/id :message/content
                              {:message/from [:user/username]}]) ...]
             :where [?e :message/content]]
        (d/db conn))))

  (with-conn conn
    @(d/transact conn
       [[:db/retract 17592186045427 :user/username "狗好看"]])))
