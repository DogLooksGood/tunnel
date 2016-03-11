(ns tunnel.system
  (:require [figwheel-sidecar.system :as sys]
            [dev.datomic :refer [new-database]]
            [taoensso.sente.server-adapters.http-kit
             :refer (sente-web-server-adapter)]
            [system.components
             [sente :refer [new-channel-sockets]]]
            [system.core :refer [defsystem]]
            [tunnel.handler :as hdlr]
            [tunnel.subs :as subs]
            [tunnel.db :as db]))

;; 先只写一个测试环境的.
(defsystem dev-system
  [:sente (new-channel-sockets hdlr/event-msg-handler* sente-web-server-adapter)
   :figwheel-system (sys/figwheel-system (sys/fetch-config))
   :datomic (new-database  "datomic:mem://localhost:4334/test")
   :tx-listener (db/tx-listener #'subs/parse-tx)])
