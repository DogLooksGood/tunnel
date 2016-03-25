(ns tunnel.core
  (:require [hiccup.core :as html :refer [html]]
            [reloaded.repl :refer [system]]
            [hiccup.page :refer [include-js include-css html5]]
            [org.httpkit.server]
            [ring.middleware
             [defaults]
             [session]
             [keyword-params]
             [params]
             [stacktrace]]
            [ring.util.response :refer [redirect]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [tunnel.parser]
            [tunnel.query]
            [tunnel.mutate]
            [tunnel.handler :as hdlr]
            [tunnel.page :as page]
            [compojure.core :refer [defroutes GET POST]]))

;; =============================================================================
;; Route

(defroutes route
  (GET "/" req page/index-page)
  (GET "/login" req page/login-page)
  (GET "/register" req page/register-page)
  (POST "/api/:key" req hdlr/api-handler*)
  (GET "/chsk" req ((-> system :sente :ring-ajax-get-or-ws-handshake) req))
  (POST "/chsk" req ((-> system :sente :ring-ajax-post) req)))

;; =============================================================================
;; Handler

(def ring-handler (-> #'route
                    ;; 先用最简单的session方案.
                    ;; ring.middleware.session/wrap-session
                    (ring.middleware.defaults/wrap-defaults
                      ring.middleware.defaults/site-defaults)
                    ring.middleware.stacktrace/wrap-stacktrace))

#_(org.httpkit.server/run-server ring-handler
   {:port 3456})



