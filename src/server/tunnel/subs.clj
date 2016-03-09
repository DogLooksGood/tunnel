(ns tunnel.subs
  "服务器端的数据推送服务的订阅, 和取消订阅.
  对于每一组`key+selector+params`, 得到tx的时候进行一次计算.
  然后将计算结果发送给所有相关的`uid`对应的客户端."
  (:require [tunnel.utils :as utils]
            [taoensso.timbre :refer [spy]]))
;; =============================================================================
;; Helpers

(defn- disj-or-dissoc
  [m k v]
  (if-let [s (get m k)]
    (if (and (contains? s v)
          (= 1 (count s)))
      (dissoc m k)
      (update m k disj v))
    m))

(defn- disj-or-dissoc-in
  [m ks k v]
  (update-in m ks disj-or-dissoc k v))

;; =============================================================================
;; States

;; 为了调试方便
;; TODO defonce
(def ^:private uid<->sub (atom {:uid->sub {}
                      :sub->uid {}}))

;; =============================================================================
;; Register/Unregister

(defn register-sub
  "注册一个客户端`uid`, 针对一个查询`key+selector+params`的订阅."
  [uid key selector params]
  (let [sub [key selector params]]
    (swap! uid<->sub
      (comp
        #(update-in % [:uid->sub uid] clojure.set/union #{sub})
        #(update-in % [:sub->uid sub] clojure.set/union #{uid})))))

(defn unregister-sub
  "注销一个客户端的订阅."
  [uid key selector params]
  (let [sub [key selector params]]
    (swap! uid<->sub
      (comp
        #(disj-or-dissoc-in % [:uid->sub] uid sub)
        #(disj-or-dissoc-in % [:sub->uid] sub uid)))))

(defn unregister-all-subs
  "注销一个用户所有的订阅"
  [uid]
  (let [subs (get-in @uid<->sub [:uid->sub uid] #{})]
    (swap! uid<->sub
      (comp
        #(update-in % [:uid->sub] dissoc uid)
        (fn [m]
          (reduce
            #(disj-or-dissoc-in %1 [:sub->uid] %2 uid)
            m
            subs))))))

(defn uid->sub
  []
  (:uid->sub @uid<->sub))

(defn- debug-uid<->sub []
  (clojure.pprint/pprint
    @uid<->sub))
