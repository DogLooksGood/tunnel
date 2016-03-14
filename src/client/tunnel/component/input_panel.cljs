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
  [e]
  (when (and (= 13 (.-keyCode e))
          (or (.-ctrlKey e) (.-metaKey e)))
    (let [v (.-value (gdom/getElement "msg-input"))]
      (when-not (empty? v)
        (remote/command [:user/send-message {:content v}])
        (set! (.-value (gdom/getElement "msg-input")) "")))))

(defn input-panel
  []
  [:div.input-panel
   [:textarea#msg-input {:rows 1
                         :on-change resize-textarea
                         :on-key-down input-key-down}]])


