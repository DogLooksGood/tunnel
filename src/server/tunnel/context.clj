(ns tunnel.context
  "请求中的上下文相关数据的存储和获取.")

;; =============================================================================
;; Dynamics

(def ^:dynamic *request* nil)
(def ^:dynamic *cookie* nil)
(def ^:dynamic *session* nil)
(def ^:dynamic *uid* nil)
(def ^:dynamic *params* nil)

(defmacro with-context
  "绑定请求中的有意义的参数, 方便后面获取."
  [req & body]
  `(binding [*request* ~req
             *session* (:session ~req)
             *cookie* (:cookie ~req)
             *uid* (-> ~req :session :uid)
             *params* (:params ~req)]
     (do
       ~@body)))

(defn current-request []
  *request*)

(defn current-cookie []
  *cookie*)

(defn current-session []
  *session*)

(defn current-uid []
  *uid*)

(defn current-params []
  *params*)

;; Usage:
(comment
  (with-context {:session {:uid 10}}
    (prn (current-uid))
    (prn (current-session))))

