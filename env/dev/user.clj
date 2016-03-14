(ns user
  "默认的NS, REPL启动的时候自动加载本文件."
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [figwheel-sidecar.system :as sys]
            [tunnel.system :refer [dev-system]]))

(reloaded.repl/set-init! dev-system)

(defn cljs-repl
  "用指定的Figwheel的环境来切换CLJS的REPL"
  []
  (sys/cljs-repl (:figwheel-system system)))


