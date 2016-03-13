(ns tunnel.socket
  "和websocket相关的代码."
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]
   [taoensso.timbre :refer [info debug spy]])
  (:require
   [tunnel.state :as state]
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]))

;; event channel, 在websocket连接成功之后开始消费.
;; 由于UI会在页面加载的时候立刻开始渲染, 而websocket需要等待连接成功.
;; 所以UI产生的需要websocket处理的事件统一发送到这个channel.
(defonce ch-ev (chan))

;; ==============================================================================
;; WebSocket Init

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
        {:type :auto                    ; e/o #{:auto :ajax :ws}
         :wrap-recv-evs? false          ; 不自动的为服务器推送的数据添加默认event-id
         })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(defmulti event-msg-handler
  (fn [ev-id ev-msg] ev-id))

(defn send!
  [ev cb]
  (put! ch-ev {:ev ev :cb cb}))

;; (send! [:msg/test {:text "hello, world"}])

(defn consume-ch-ev
  "消费ch-ev中的事件,
  TODO 处理回调函数."
  []
  (go-loop []
    (let [{:keys [ev cb]} (<! ch-ev)]
      (debug "chsk-send: " ev)
      (if cb
        (chsk-send! ev 1000 cb)
        (chsk-send! ev))
      (recur))))

(defmethod event-msg-handler :chsk/state
  [ev-id ev-msg]
  (let [{:keys [open? first-open?]} ev-msg]
    (when (and open? first-open?)
      (consume-ch-ev))))

;; 服务器向客户端推送数据的事件.
(defmethod event-msg-handler :system/pub
  [ev-id ev-msg]
  (state/merge! ev-msg))

(defmethod event-msg-handler :default
  [ev-id ev-msg]
  )

(defn event-msg-handler*
  [{:keys [event]}]
  (info (first event) (second event))
  (event-msg-handler (first event) (second event)))

;; 用event-msg-handler代替prn, event-msg-handler: (event)
(sente/start-client-chsk-router! ch-chsk
  event-msg-handler*)

