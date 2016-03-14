(ns tunnel.datomic
  "测试环境用的datomic组件, 没有用system中自带的.
  这个组件在启动的时候, 会加载`data/schema.edn`和`data/initial.edn`中的内容."
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [clojure.java.io :as io])
  (:import datomic.Util))

;; =============================================================================
;; Component
(defrecord DatomicDatabase [uri schema initial-data connection]
  component/Lifecycle
  (start [component]
    (d/create-database uri)
    (let [c (d/connect uri)]
      @(d/transact c schema)
      @(d/transact c initial-data)
      (assoc component :connection c)))
  (stop [component]
    (d/delete-database uri)
    (assoc component :connection nil)))

(defn new-database [db-uri]
  (DatomicDatabase.
    db-uri
    (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
    (first (Util/readAll (io/reader (io/resource "data/initial.edn"))))
    nil))
