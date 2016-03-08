(ns tunnel.subs
  "客户端的订阅, 可以订阅前端本地数据和服务器数据.")

(defn register-remote-sub
  "订阅一个远程的查询"
  [key selector params])

(defn unregister-remote-sub
  "取消订阅一个远程的查询"
  [key selector params])







