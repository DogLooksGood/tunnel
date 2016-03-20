(ns tunnel.client-utils
  (:require [goog.dom :as gdom]
            [goog.dom.classlist :as cls]))

(defn animate
  [id a]
  (let [dom (gdom/getElement id)]
    (.addEventListener dom "webkitAnimationEnd"
      (fn [e]
        (.removeEventListener dom "webkitAnimationEnd")
        (cls/removeAll dom #js [a "animated"])))
    (cls/addAll dom #js [a "animated"])))
