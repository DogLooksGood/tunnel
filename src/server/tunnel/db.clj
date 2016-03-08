(ns tunnel.db
  (:require [datomic.api :as d]))

(defmacro with-conn
  [conn & exprs]
  `(let [~conn (d/connect "datomic:mem://localhost:4334/test")]
     (do ~@exprs)))

(defmulti query
  (fn [key selector params]
    key))

(defmulti mutate
  (fn [key params]
    key))

;; =============================================================================
;; Transaction Listener

(defonce tx-listener (atom nil))

(defn start-tx-listener
  "开始监听tx-report, 对每一个tx调用callback.
  只能启动一次."
  [callback]
  {:pre [(nil? @tx-listener)]}
  (reset! tx-listener
    (with-conn conn
      (let [tx-report (d/tx-report-queue conn)]
        (future
          (loop []
            (let [tx (.take tx-report)]
              (callback tx)
              (recur))))))))

(defn stop-tx-listener
  "停止监听tx-report."
  []
  (future-cancel @tx-listener)
  (reset! tx-listener nil))

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

(defmethod mutate :user/set-status
  [key params]
  (let [{:keys [user/status db/id]} params]
    (with-conn conn
      @(d/transact conn
         [[:db/add id :user/status :online]]))))

(defmethod mutate :user/add-tag
  [key params]
  (let [{:keys [db/id user/tags]} params]
    (with-conn conn
      @(d/transact conn
         (mapv #(do [:db/add id :user/tags %]) tags)))))

