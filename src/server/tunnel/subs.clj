(ns tunnel.subs
  "服务器端的数据推送服务的订阅, 和取消订阅.
  对于每一组`key+selector+params`, 得到tx的时候进行一次计算.
  然后将计算结果发送给所有相关的`uid`对应的客户端.")

(defn register-sub
  "注册一个客户端`uid`, 针对一个查询`key+selector+params`的订阅."
  [uid key selector params])

(defn unregister-sub
  "注销一个客户端的订阅."
  [uid key selector params])
