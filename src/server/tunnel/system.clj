(ns tunnel.system
  (:require [figwheel-sidecar.system :as sys]
            [dev.datomic :refer [new-database]]
            [taoensso.sente.server-adapters.http-kit
             :refer (sente-web-server-adapter)]
            [system.components
             [sente :refer [new-channel-sockets]]]
            [system.core :refer [defsystem]]))

;; 先只写一个测试环境的.
(defsystem dev-system
  [:sente (new-channel-sockets #(prn %) sente-web-server-adapter) ; #(prn %) 这里换自定义的event-msg-handler [event]
   :figwheel-system (sys/figwheel-system (sys/fetch-config))
   :datomic (new-database  "datomic:mem://localhost:4334/test")])
