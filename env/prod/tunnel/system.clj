(ns tunnel.system
  (:require [tunnel.datomic :refer [new-database]]
            [taoensso.sente.server-adapters.http-kit
             :refer (sente-web-server-adapter)]
            [system.components
             [sente :refer [new-channel-sockets]]
             [http-kit :refer [new-web-server]]]
            [system.core :refer [defsystem]]
            [tunnel.core :refer [ring-handler]]
            [tunnel.handler :as hdlr]
            [tunnel.subs :as subs]
            [tunnel.db :as db]))

;; 正式环境的.
(defsystem prod-system
  [:sente (new-channel-sockets hdlr/event-msg-handler* sente-web-server-adapter)
   :datomic (new-database  "datomic:mem://localhost:4334/test")
   :server (new-web-server 9999 ring-handler)
   :tx-listener (db/tx-listener subs/parse-tx)])
