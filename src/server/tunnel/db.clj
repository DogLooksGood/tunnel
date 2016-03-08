(ns tunnel.db
  (:require [datomic.api :as d]))

(defmacro with-conn
  [conn & exprs]
  `(let [~conn (d/connect "datomic:mem://localhost:4334/test")]
     (do ~@exprs)))

(defmulti query
  (fn [key selector params]
    key))

(defmethod query :user-login
  [key selector params]
  (let [{:keys [username password]} params]
    (with-conn conn
      (d/q '[:find (pull ?e selector) .
             :in $ selector ?u ?p
             :where
             [?e :user/username ?u]
             [?e :user/password ?p]]
        (d/db conn) selector username password))))
