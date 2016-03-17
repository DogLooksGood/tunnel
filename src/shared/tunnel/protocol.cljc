(ns tunnel.protocol
  (:require [schema.core :as s]))

(def app-query (atom {}))
(def app-command (atom {}))

(defn register-query
  [kw schema & [opt]]
  {:pre [(keyword? kw)]}
  (swap! app-query assoc kw {:key kw
                             :schema schema}))

(defn register-command
  [kw schema & [{:keys [cb-initialized
                        cb-accepted
                        cb-processed
                        cb-failed] :as opt}]]
  {:pre [(keyword? kw)]}
  (swap! app-command assoc kw (merge {:key kw
                                       :schema schema}
                                 opt)))
(defn kw->command
  [kw]
  {:pre [(keyword? kw)]
   :post [(not (nil? %))]}
  (get @app-command kw))

(defn kw->query
  [kw]
  {:pre [(keyword? kw)]
   :post [(not (nil? %))]}
  (get @app-query kw))

(register-query :user/q {:name s/Str
                         :sel s/Any})

(register-command :user/c {:name s/Str})

;; (prn @app-query)


