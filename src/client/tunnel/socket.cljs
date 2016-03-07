(ns tunnel.socket ; .cljs
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   ;; <other stuff>
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)] ; <--- Add this
  ))

;;; Add this: --->
(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" ; Note the same path as before
        {:type :auto                      ; e/o #{:auto :ajax :ws}
         })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)           ; ChannelSocket's receive channel
  (def chsk-send! send-fn)           ; ChannelSocket's send API fn
  (def chsk-state state)             ; Watchable, read-only atom
  )

;; 用event-msg-handler代替prn, event-msg-handler: (event)
(sente/start-client-chsk-router! ch-chsk
  #(prn (:event %)))
