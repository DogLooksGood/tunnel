(ns tunnel.component.input-panel
  (:require [goog.dom :as gdom]
            [tunnel.remote :as remote]))

(defn- resize-textarea
  []
  (let [e (gdom/getElement "msg-input")
        v (.-value e)
        n (inc (count (filter #(= % \newline) v)))]
    (set! (.-rows e) n)))

(defn- input-key-down
  [ev]
  (when (and (= 13 (.-keyCode ev))
          (or (.-ctrlKey ev) (.-metaKey ev)))
    (let [e (gdom/getElement "msg-input")
          v (.-value e)]
      (when-not (empty? v)
        (remote/command [:user/send-message {:content v}])
        (set! (.-value e) "")
        (set! (.-rows e) 1)))))

(defn input-panel
  []
  [:div.input-panel
   [:textarea#msg-input {:rows 1
                         :on-change resize-textarea
                         :on-key-down input-key-down}]])


