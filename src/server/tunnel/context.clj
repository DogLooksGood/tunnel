(ns tunnel.context
  "请求中的上下文相关数据的存储和获取.")

;; =============================================================================
;; Dynamics

(def ^:dynamic *request* nil)

(defmacro with-context
  "绑定请求中的有意义的参数, 方便后面获取."
  [req & body]
  `(binding [*request* ~req]
     (do
       ~@body)))

(defn current-request []
  *request*)

(defmacro with-request
  [req & body]
  `(let [~req (current-request)]
     (do
       ~@body)))

;; Usage:
(comment
  (with-context {:session {:uid 10}}
    (with-request req
      (prn req))))

