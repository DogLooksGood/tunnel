(ns tunnel.remote
  "客户端的订阅, 可以订阅前端本地数据和服务器数据."
  (:require [taoensso.timbre :refer-macros [spy debug]]
            [schema.core :as s :include-macros true]
            [tunnel.socket :as socket]
            [tunnel.protocol :refer [kw->command kw->query]]
            [tunnel.state :as state]))

;; Reloadable 
(defonce sub->count (atom {}))

;; =============================================================================
;; Helpers

(defn- sub-count-inc
  [m expr]
  (if (contains? m expr)
    (update m expr inc)
    (do
      (println "Register sub: " expr)
      (socket/send! (spy [:user/subscribe expr]) state/merge!)
      (assoc m expr 1))))

(defn- sub-count-dec
  "TODO, state gc"
  [m expr]
  (if (= 1 (m expr))
    (do
      (println "Unregister sub: " expr)
      (socket/send! [:user/unsubscribe expr] nil)
      (dissoc m expr))
    (update m expr dec)))

;; =============================================================================
;; Register/Unregister
;; (defn command
;;   "发送一个命令."
;;   [[key params]]
;;   (let [expr [key params]]
;;     (socket/send! [:user/command expr] nil)))

;; (defn unregister-sub
;;   "取消订阅一个远程的查询"
;;   [[key selector params]]
;;   (let [expr [key selector params]]
;;     (swap! sub->count sub-count-dec expr)))

;; (defn fetch-
;;   "直接请求一个查询.
;;   TODO 实现可选参数: 是否缓存."
;;   [[key selector params] & [{:keys [cache]}]]
;;   (let [expr [key selector params]]
;;     (socket/send! [:user/fetch expr] state/merge!)))

;; (defn register-sub
;;   "先查询, 然后再订阅这个查询"
;;   [[key selector params]]
;;   (let [expr [key selector params]]
;;     (swap! sub->count sub-count-inc expr)))

;; =============================================================================
;; 新实现

(defn fetch
  "发送一个查询, 只运行一次."
  [[q params :as expr]]
  (let [{:keys [key schema]} (kw->query q)]
    (when schema
      (s/validate schema params))
    (socket/send! [:user/fetch expr]
      state/merge!)))

(defn subscribe
  "订阅一个查询, 首先会查询返回初始化数据, 然后会实时推送数据的变化. 并合并到原数据当中."
  [[q params :as expr]]
  (let [{:keys [key schema]} (kw->query q)]
    (when schema
      (s/validate schema params))
    (swap! sub->count sub-count-inc expr)))

(defn unsubscribe
  "取消一个查询的订阅, 同时清空已经存在的数据."
  [[q params :as expr]]
  (let [{:keys [key schema]} (kw->query q)]
    (when schema
      (s/validate schema params))
    (swap! sub->count sub-count-dec expr)))

(defn dispatch
  "发送一个命令到服务器."
  [[c params :as expr]]
  (let [{:keys [key schema]} (kw->command c)]
    (when schema
      (s/validate schema params))
    (let [command (state/create-command! key params)]
      (socket/send! [:user/command command]
        state/update-command!))))

(defn re-subscribe-all
  "重新订阅所有, 用于断线重连之后."
  []
  (doseq [expr (keys @sub->count)]
    (socket/send! [:user/subscribe expr]
      state/merge!)))

(reset! socket/reconnect-callback re-subscribe-all)

