(defproject tunnel "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"
  
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [com.taoensso/sente "1.8.1"]
                 [org.danielsz/system "0.2.0"]
                 [reagent "0.6.0-alpha"]
                 [garden "1.3.2"]
                 [compojure "1.4.0"]
                 [secretary "1.2.3"]
                 [datascript "0.15.0"]
                 [com.datomic/datomic-free "0.9.5350"]
                 ;; DEV 后期挪出去
                 [figwheel-sidecar "0.5.0"]]
  
  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src/server" "src/shared" "env"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/client"]

                :figwheel true

                :compiler {:main tunnel.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/tunnel.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/tunnel.js"
                           :main tunnel.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:http-server-root "public"
             ;; :css-dirs ["resources/public/css"]
             :ring-handler tunnel.core/ring-handler
             :server-logfile "/tmp/figwheel-logfile.log"
             })
