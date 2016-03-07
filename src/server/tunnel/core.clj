(ns tunnel.core
  (:require [hiccup.core :as html :refer [html]]
            [reloaded.repl :refer [system]]
            [hiccup.page :refer [include-js include-css]]
            [ring.middleware
             [session]
             [keyword-params]
             [params]]
            [compojure.core :refer [defroutes GET POST]]))

(defn index-page
  [req]
  (html
    [:head
     (include-css "css/normalize.css")]
    [:body
     [:div#app]
     (include-js "js/compiled/tunnel.js")]))

(defroutes route
  (GET "/" req index-page)
  (GET "/chsk" req ((-> system :sente :ring-ajax-get-or-ws-handshake) req))
  (POST "/chsk" req ((-> system :sente :ring-ajax-post) req)))

(def handler (-> #'route
               ring.middleware.keyword-params/wrap-keyword-params
               ring.middleware.params/wrap-params
               ;; 先用最简单的session方案.
               ring.middleware.session/wrap-session))
