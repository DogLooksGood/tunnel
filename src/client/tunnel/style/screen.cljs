(ns tunnel.style.screen
  (:require [tunnel.style.normalize :refer [normalize]]
            [goog.style :as gs]
            [garden.core :refer [css]]
            [garden.units :refer [vh px em percent]]))

(def indigo-200 "#9FA8DA")
(def indigo-500 "#3F51B5")
(def indigo-800 "#283593")
(def grey-100 "#F5F5F5")
(def grey-300 "#E0E0E0")
(def grey-600 "#757575")

;; =============================================================================
;; Height & Width
(def header-h (em 3))
(def menu-w (em 10))

;; =============================================================================
;; Font
(def ft-3 (em 0.25))
(def ft-2 (em 0.5))
(def ft-1 (em 0.75))
(def ft0 (em 1))
(def ft+1 (em 1.25))
(def ft+2 (em 1.5))
(def ft+3 (em 1.75))

;; =============================================================================
;; Margin & Padding
(def e1 (em 0.25))
(def e2 (em 0.5))
(def e3 (em 0.75))
(def e4 (em 1))

;; 测试环境动态加载,
;; TODO 正式环境直接编译.
(defonce style (atom nil))

(when-not (nil? @style)
  (doseq [s @style]
    (gs/uninstallStyles s))
  (reset! style nil))

;; 测试
(def ^:const global
  [:body {:background-color :white
          :font {:family "sans-serif"
                 :size :large}}
   [:.list {:margin 0
            :padding 0}]
   [:.item {:margin 0
            :padding 0
            :list-style-type :none}]])

(def ^:const menu
  [:.menu {:width menu-w
           :background-color indigo-500
           :float :left
           :height (vh 100)
           :color :white}
   [:.menu-profile {:width :inherit
                    :box-sizing :border-box
                    :padding e2
                    :height header-h
                    :background-color indigo-800}
    [:.username {}]]
   [:.menu-list {:color grey-300
                 :width :inherit
                 :box-sizing :border-box
                 :padding e2
                 :padding-left 0}
    [:.menu-item {:font {:size ft-1}
                  :padding {:top e1
                            :left e2
                            :bottom e1}}
     [:&:hover {:color :black
                :background-color indigo-200
                :border {:top-right-radius (px 5)
                         :bottom-right-radius (px 5)
                         }}]
     [:&:before {:content "\"# \""}]]
    [:.menu-item-title {:font {:size ft-1}
                        :padding {:top e1
                                  :left e2
                                  :bottom e1}}]]])

(def ^:const content
  [:.content {:height (vh 100)
              :overflow :hidden
              :position :relative}
   [:.header {:height header-h
              :background-color grey-100}]])

(def ^:const input-panel
  [:.input-panel {:position :absolute
                  :width (percent 100)
                  :text-align :center
                  :bottom 0}
   [:textarea {:resize :none
               :width (percent 80)
               :outline :none
               :border {:radius (px 5)
                        :color grey-600}
               :font {:size ft0}
               :margin {:left :auto :right :auto
                        :bottom e4}}]])

(def ^:const msg-panel
  [:.msg-panel {:margin e1
                :height (vh 80)
                :overflow :scroll}
   [:.msg
    [:.msg-from {:color grey-600
                 :padding e1}
     ]
    [:.msg-content {:border {:radius (px 10)
                             :color grey-100}
                    :padding e3
                    :margin {:left e2}
                    :display :inline-block
                    :background-color grey-100}]]])

(def root
  `[~@global
    ~menu
    ~content
    ~input-panel
    ~msg-panel])

(reset! style [(gs/installStyles (css normalize))
               (gs/installStyles (css root))])
