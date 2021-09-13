(defproject tickets "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.879" :scope "provided"]
                 [org.clojure/spec.alpha "0.2.194"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [ring "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.1"]
                 [bk/ring-gzip "0.3.0"]
                 [radicalzephyr/ring.middleware.logger "0.6.0"]
                 [clj-logging-config "1.9.12"]
                 [slingshot/slingshot "0.12.2"]
                 [environ "1.2.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.danielsz/system "0.4.7"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [compojure "1.6.2"]
                 [cheshire "5.10.0"]
                 [re-frame "1.2.0"]
                 [bidi "2.1.6"]
                 [kibu/pushy "0.3.8"]
                 [day8.re-frame/http-fx "0.2.3"]
                 [cljs-ajax "0.8.4"]
                 [com.datomic/dev-local "0.9.235"]
                 [org.clojure/core.async "1.3.618"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-environ "1.1.0"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/assets/js" "dev-target"]

  :uberjar-name "testapp.jar"

  ;; Use `lein run` if you just want to start a HTTP server, without figwheel
  :main tickets.application

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (go) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}

  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc" "dev"]

                :figwheel {:on-jsload "tickets.system/reset"}

                :compiler {:main cljs.user
                           :asset-path "/assets/js/compiled/out"
                           :output-to "dev-target/public/assets/js/compiled/tickets.js"
                           :output-dir "dev-target/public/assets/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [re-frisk.preload]
                           :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}}}

               {:id "test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "dev-target/public/assets/js/compiled/testable.js"
                           :main tickets.test-runner
                           :optimizations :none}}

               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main tickets.system
                           :output-to "resources/public/js/compiled/tickets.js"
                           :output-dir "target"
                           :source-map-timestamp true
                           :optimizations :advanced
                           :closure-defines {goog.DEBUG false}
                           :pretty-print false}}]}

  ;; When running figwheel from nREPL, figwheel will read this configuration
  ;; stanza, but it will read it without passing through leiningen's profile
  ;; merging. So don't put a :figwheel section under the :dev profile, it will
  ;; not be picked up, instead configure figwheel here on the top level.

  :figwheel {;; :http-server-root "public"       ;; serve static assets from resources/public/
             ;; :server-port 3449                ;; default
             ;; :server-ip "127.0.0.1"           ;; default
             :css-dirs ["resources/public/assets/css"]  ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process. We
             ;; don't do this, instead we do the opposite, running figwheel from
             ;; an nREPL process, see
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             ;; :nrepl-port 7888

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             :server-logfile "log/figwheel.log"}

  :doo {:build "test"}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.20"]
                             [figwheel-sidecar "0.5.20"]
                             [cider/piggieback "0.4.0"]
                             [cider/cider-nrepl "0.18.0"]
                             [lein-doo "0.1.11"]
                             [reloaded.repl "0.2.4"]
                             [re-frisk "1.5.1"]
                             [re-frisk-remote "1.5.1"]
                             [clj-http "3.12.3"]
                             [etaoin "0.4.6"]]

              :plugins [[lein-figwheel "0.5.18"]
                        [lein-doo "0.1.11"]]

              :source-paths ["dev"]
              :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}

             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile"
                           ["cljsbuild" "once" "min"]]
              :hooks []
              :omit-source true
              :aot :all}})
