(ns tunnel.state
  "前端的状态使用DataScript来存储."
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [tunnel.utils :as utils]
            [taoensso.timbre :refer-macros [debug]]))

;; 本地的状态, 使用自定义的key.
;; 远程的状态, 使用 ::remote -> params 两级的key.
;; 命令, 使用 ::command 为key. 保留最近30个.
(defonce app-state (r/atom {::remote {}
                            ::command []}))

(let [curr-id (atom 0)]
  (defn- gen-tempid
    []
    (swap! curr-id inc)))

(defn- append-command-fn
  [xs x]
  (take-last 10
    (conj xs x)))

(defn create-command!
  [key params]
  (let [tempid (gen-tempid)
        command {:key key
                 :status :initialized
                 :params params
                 :tempid tempid}]
    (swap! app-state update-in [::command] append-command-fn command)
    command))

(defn- merge-delta
  [expr delta]
  (swap! app-state update-in [::remote expr] utils/join delta))

(defn- merge-value
  [expr value]
  (swap! app-state assoc-in [::remote expr] value))

(defn- update-command-fn
  [xs tempid status]
  (mapv (fn [x]
          (if (= tempid (:tempid x))
            (assoc x :status status)
            x))
    xs))

(defn update-command!
  [{:keys [status tempid] :as data}]
  (swap! app-state update-in [::command] update-command-fn tempid status))

(defn merge!
  "合并服务器返回的数据到app-state.
  返回值中的delta会应用到原来的数据上, 返回值中的value会覆盖原来的数据.
  delta和value不同时出现.
  TODO: 逻辑有点乱"
  [{:keys [delta value expr] :as data}]
  (prn "merge: " expr data)
  (cond
    value (merge-value expr value)
    delta (merge-delta expr delta)))

(defn log-state
  []
  (prn "state:" @app-state))

;; =============================================================================
;; Query functions

(defn local
  "查询本地的状态, 传入一个路径."
  [ks]
  (reaction (get-in @app-state ks)))

;; (defn remote
;;   "从服务器返回的数据, 传查询的表达式[key selector params]"
;;   [expr]
;;   (reaction (get-in @app-state [::remote expr])))

(defn remote
  [[key params :as expr]]
  (reaction (get-in @app-state [::remote expr])))


