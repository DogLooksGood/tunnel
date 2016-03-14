(ns user
  "默认的NS, REPL启动的时候自动加载本文件."
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [tunnel.system :refer [prod-system]])
  (:gen-class))


(defn -main [& args]
  (reloaded.repl/set-init! prod-system)
  (go))
