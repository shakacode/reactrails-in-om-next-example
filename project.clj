(defproject omnext-to-datomic "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/test.check "0.8.2"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.cognitect/transit-cljs "0.8.225"]
                 [com.datomic/datomic-free "0.9.5344"]
                 ; server
                 [ring-transit "0.1.4"]
                 [http-kit "2.1.18"]
                 [compojure "1.4.0"]
                 ;; front end
                 [cljs-ajax "0.5.2"]
                 [org.omcljs/om "1.0.0-alpha28"]
                 [sablono "0.5.1"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [cljsjs/react-dom-server "0.14.3-0"]
                 ;; for deployment
                 [environ "1.0.1"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-mock "0.3.0"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1" :exclusions [org.clojure/clojure org.clojure/tools.reader] ]
            [lein-ring "0.9.7"]
            [lein-environ "1.0.1"]]

  :min-lein-version "2.0.0"
  
  :uberjar-name "heroku-omnext-to-datomic.jar"

  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]
                                  ]}
             :production {:env {:production true}}
             :uberjar {:hooks [leiningen.cljsbuild]
                       :aot :all
                       :env {:production true}
                       :cljsbuild  {:builds [  {:app
                                                {:source-paths ["src/cljs"]
                                                 :compiler {:optimizations :advanced
                                                            :pretty-print false}}}]}}}

  :main omnext-to-datomic.handler

  :source-paths ["src" "src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled/out" "target" "resources/public/compiled/omnext_to_datomic.js"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]

                :figwheel {:on-jsload "omnext-to-datomic.core/on-js-reload"}

                :compiler {:main omnext-to-datomic.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/omnext_to_datomic.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src/cljs/"]
                :compiler {:output-to "resources/public/js/compiled/omnext_to_datomic.js"
                           :main omnext-to-datomic.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
