(ns tunnel.entry
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [tunnel.system :refer [prod-system]])
  (:gen-class))


(defn -main [& args]
  (reloaded.repl/set-init! prod-system)
  (go))
