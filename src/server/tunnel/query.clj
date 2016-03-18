(ns tunnel.query
  (:require
   [taoensso.timbre :refer [debug spy]]
   [tunnel.parser :refer [query tx-query]]
   [datomic.api :as d]))

(defmethod query :user/who-am-i
  [{:keys [uid db]} _ _]
  (when uid
    (d/pull db '[:db/id :user/username] uid)))

(defmethod query :user/list-all
  [{:keys [db]} _ _]
  (d/q '[:find [(pull ?e [*]) ...]
         :where
         [?e :user/username]]
    db))

(defmethod tx-query :user/list-all
  [{:keys [db tx-data]} _ _]
  (d/q '[:find [(pull ?e [*]) ...]
         :in $ $tx-data
         :where
         [$tx-data ?e]
         [?e :user/username]]
    db tx-data))

(defmethod query :message/list-all
  [{:keys [db]} _ {:keys [sel]}]
  (->> (d/datoms db
         :aevt :message/from)
    reverse
    (take 30)
    (mapv #(d/pull db sel (:e %)))))

(defmethod tx-query :message/list-all
  [{:keys [db tx-data]} _ {:keys [sel]}]
  (d/q '[:find [(pull ?e sel) ...]
         :in $ $tx-data sel
         :where
         [$tx-data ?e]
         [?e :message/from]]
    db tx-data sel))
