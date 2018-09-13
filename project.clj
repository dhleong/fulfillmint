(defproject fulfillmint "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.329"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.5"]
                 [secretary "1.2.3"]

                 [kibu/pushy "0.3.8"]
                 [cljs-ajax "0.7.3"]

                 ; data querying and persistence
                 [datascript "0.16.6"]
                 [datascript-transit "0.2.2"
                  :exclusions [com.cognitect/transit-cljs]]
                 [com.cognitect/transit-cljs "0.8.256"]

                 [alandipert/storage-atom "2.0.1"]

                 ; simpler forms
                 [reagent-forms "0.5.42"]

                 ]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-less "1.7.5"]
            [lein-npm "0.6.2"]]

  ; npm is only needed for installing test dependencies
  :npm {:devDependencies [[karma "2.0.3"]
                          [karma-cljs-test "0.1.0"]
                          [karma-chrome-launcher "2.2.0"]]}

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7002
             :server-ip "0.0.0.0"
             :ring-handler wish.dev-server/http-handler
             :nrepl-middleware
             [cemerick.piggieback/wrap-cljs-repl cider.nrepl/cider-middleware]}

  :less {:source-paths ["less"]
         :target-path  "resources/public/css"}

  ;; :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :aliases {"build" ["with-profile" "+prod,-dev" "do"
                     ["clean"]
                     ["cljsbuild" "once" "min"]
                     ["less" "once"]]
            "test" ["do" "test"
                         ["doo" "chrome-headless" "test" "once"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3-react16"]
                   [day8.re-frame/tracing "0.5.1"]
                   [figwheel-sidecar "0.5.16"]
                   ;; [cider/piggieback "0.3.6"]
                   [com.cemerick/piggieback "0.2.2"]]

    :source-paths ["src/cljs"]

    :plugins      [[lein-figwheel "0.5.16"]
                   [lein-doo "0.1.8"]]}

   :prod { :dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "fulfillmint.core/mount-root"}
     :compiler     {:main                 fulfillmint.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true
                                           fulfillmint.util.nav.LOCAL true}
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            fulfillmint.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          fulfillmint.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}
    ]}
  )
