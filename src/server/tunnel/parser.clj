(ns tunnel.parser
  "查询和命令的接口"
  (:require [tunnel.db :as db]
            [tunnel.utils :as utils]))

(defn- dispatch
  [env key params] key)

;; 查询
;; env: {:keys [conn db ring-req uid]}
;; key: keyword, 查询id
;; params: hash-map, 查询的参数.
;; 返回
;; {:keys [value]}  value为查询结果

(defmulti query dispatch)

(defmethod query :default
  [_ _ _])

;; 通过事务增量查询
;; env: {:keys [db uid tx-data]}
;; key: keyword, 查询id
;; params: hash-map, 查询的参数.
;; 返回
;; {:keys [delta]}  delta为对应查询在tx-data影响下的变化

(defmulti tx-query dispatch)

(defmethod tx-query :default
  [_ _ _])

;; 修改
;; env: {:keys [conn db ring-req uid]}
;; key: keyword, 命令id
;; params: hash-map, 命令的参数.
;; 返回
;; {:keys [status]}  status为命令当前的处理进度.
;;   status取值:
;;     :initialized 已初始化
;;     :accepted    已接受
;;     :processed   已处理
;;     :failed      已失败

(defmulti mutate dispatch)

(defmethod mutate :default
  [_ _ _])

(defn diff-result
  [key params tx]
  (let [db-before (:db-before tx)
        db-after (:db-after tx)
        tx-data (:tx-data tx)
        before (tx-query {:db db-before :tx-data tx-data}
                 key params)
        after (tx-query {:db db-after :tx-data tx-data}
                key params)]
    (utils/diff :db/id before after)))
