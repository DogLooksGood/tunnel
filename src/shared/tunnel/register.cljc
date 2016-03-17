(ns tunnel.register
  "注册所有的命令和查询"
  (:require [tunnel.protocol :refer [register-query register-command]]
            [schema.core :as s #?@(:cljs [:include-macros true])]))

;; =============================================================================
;; 查询

;; 列出所有用户
(register-query :user/list-all nil)

;; 列出所有消息
(register-query :message/list-all s/Any)

;; =============================================================================
;; 命令

;; 发送消息
(register-command :message/send {:content s/Str})
