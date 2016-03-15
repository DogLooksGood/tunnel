(ns tunnel.parser
  "查询和命令的接口")

(defn- dispatch
  [env key params] key)

;; 查询
;; env: {:keys [conn db ring-req uid]}
;; key: keyword, 查询id
;; params: hash-map, 查询的参数.
;; 返回
;; {:keys [value]}  value为查询结果
(defmulti query dispatch)

;; 通过事务增量查询
;; env: {:keys [db uid tx-data]}
;; key: keyword, 查询id
;; params: hash-map, 查询的参数.
;; 返回
;; {:keys [delta]}  delta为对应查询在tx-data影响下的变化
(defmulti tx-query dispatch)

;; 修改
;; env: {:keys [conn db ring-req uid]}
;; key: keyword, 命令id
;; params: hash-map, 命令的参数.
;; 返回
;; {:keys [status]}  status为命令当前的处理进度.
(defmulti command dispatch)


