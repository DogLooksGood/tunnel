(ns tunnel.subs
  "客户端的订阅, 可以订阅前端本地数据和服务器数据."
  (:require [tunnel.socket :as socket]))

;; Reloadable 
(defonce sub->count (atom {}))

;; =============================================================================
;; Helpers

(defn- sub-count-inc
  [m sub]
  (if (contains? m sub)
    (update m sub inc)
    (do
      (println "Register sub: " sub)
      (socket/send! [:user/register-sub sub])
      (assoc m sub 1))))

(defn- sub-count-dec
  [m sub]
  (if (= 1 (m sub))
    (do
      (println "Unregister sub: " sub)
      (socket/send! [:user/unregister-sub sub])
      (dissoc m sub))
    (update m sub dec)))

;; =============================================================================
;; Register/Unregister

(defn register-remote-sub
  "订阅一个远程的查询"
  [key selector params]
  (let [sub [key selector params]]
    (swap! sub->count sub-count-inc sub)))

(defn unregister-remote-sub
  "取消订阅一个远程的查询"
  [key selector params]
  (let [sub [key selector params]]
    (swap! sub->count sub-count-dec sub)))







