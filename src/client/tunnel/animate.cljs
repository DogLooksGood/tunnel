(ns tunnel.animate
  (:require [goog.dom :as gdom]
            [goog.dom.classlist :as cls]))

(defn animate
  [dom a]
  (.addEventListener dom "webkitAnimationEnd"
      (fn [e]
        (.removeEventListener dom "webkitAnimationEnd")
        (cls/removeAll dom #js [a "animated"])))
    (cls/addAll dom #js [a "animated"]))

(defn animate-with-id
  [id a]
  (let [dom (gdom/getElement id)]
    (animate dom a)))
