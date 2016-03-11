(ns tunnel.style.screen
  (:require [tunnel.style.normalize :refer [normalize]]
            [goog.style :as gs]
            [garden.core :refer [css]]
            [garden.units :refer [px]]))

;; 测试环境动态加载,
;; TODO 正式环境直接编译.
(defonce style (atom nil))

(when-not (nil? @style)
  (doseq [s @style]
    (gs/uninstallStyles s))
  (reset! style nil))

;; 测试
(def ^:const global
  [:body {:background-color "#398093"
          :font {:family "Comic Sans MS"
                 :size (px 25)}}])

(def root
  `[~@global])

(reset! style [(gs/installStyles (css normalize))
               (gs/installStyles (css root))])
