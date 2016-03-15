(ns tunnel.remote
  "客户端的订阅, 可以订阅前端本地数据和服务器数据."
  (:require [taoensso.timbre :refer-macros [spy debug]]
            [tunnel.socket :as socket]
            [tunnel.state :as state]))

;; Reloadable 
(defonce sub->count (atom {}))

;; =============================================================================
;; Helpers

(defn- sub-count-inc
  "一个不太整洁的实现."
  [m expr]
  (if (contains? m expr)
    (update m expr inc)
    (do
      (println "Register sub: " expr)
      (socket/send! (spy [:user/register-sub expr]) state/merge!)
      (assoc m expr 1))))

(defn- sub-count-dec
  [m expr]
  (if (= 1 (m expr))
    (do
      (println "Unregister sub: " expr)
      (socket/send! [:user/unregister-sub expr] nil)
      (dissoc m expr))
    (update m expr dec)))

;; =============================================================================
;; Register/Unregister
(defn command
  "发送一个命令."
  [[key params]]
  (let [expr [key params]]
    (socket/send! [:user/command expr] nil)))

(defn unregister-sub
  "取消订阅一个远程的查询"
  [[key selector params]]
  (let [expr [key selector params]]
    (swap! sub->count sub-count-dec expr)))

(defn fetch
  "直接请求一个查询.
  TODO 实现可选参数: 是否缓存."
  [[key selector params] & [{:keys [cache]}]]
  (let [expr [key selector params]]
    (socket/send! [:user/fetch expr] state/merge!)))

(defn register-sub
  "先查询, 然后再订阅这个查询"
  [[key selector params]]
  (let [expr [key selector params]]
    (swap! sub->count sub-count-inc expr)))

(defn re-register-all-subs
  "重新订阅所有, 用于断线重连之后."
  []
  (doseq [expr (keys @sub->count)]
    (socket/send! [:user/register-sub expr] state/merge!)))

(reset! socket/reconnect-callback re-register-all-subs)




