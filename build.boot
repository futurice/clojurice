(def project 'app)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"src/cljs" "src/cljc" "src/clj" "test/clj" "resources"}
          :dependencies   '[[org.clojure/clojure "1.9.0"]
                            [org.clojure/clojurescript "1.10.238"]
                            [org.clojure/core.async "0.4.474"]

                            [org.immutant/immutant "2.1.9"]
                            [org.danielsz/system "0.4.2-SNAPSHOT"]
                            [org.clojure/java.jdbc "0.7.3"]
                            [org.clojure/tools.cli "0.3.5"]
                            [org.clojure/tools.logging "0.4.0"]
                            [metosin/ring-http-response "0.9.0"]
                            [compojure "1.6.0"]
                            [metosin/compojure-api "1.1.12"]
                            [ring "1.6.3"]
                            [org.clojure/tools.nrepl "0.2.13"]
                            [ring/ring-defaults "0.3.1"]
                            [ring-middleware-format "0.7.4"]
                            [prismatic/schema "1.1.9"]
                            [metosin/schema-tools "0.10.2"]
                            [hiccup "1.0.5"]
                            [org.postgresql/postgresql "42.3.1"]
                            [dev.weavejester/ragtime "0.9.0"]
                            [honeysql "0.9.2"]
                            [clj-http "3.9.0"]
                            [puppetlabs/kitchensink "2.5.2"]

                            [reagent "0.8.0"]
                            [funcool/bide "1.6.0"]
                            [cljs-http "0.1.45"]
                            [com.cognitect/transit-cljs "0.8.256"]

                            [binaryage/devtools "0.9.10"]
                            [binaryage/dirac "1.2.33"]
                            [powerlaces/boot-cljs-devtools "0.2.0"]
                            [metosin/reagent-dev-tools "0.2.0"]

                            [adzerk/boot-reload "0.5.2" :scope "test"]
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [adzerk/boot-cljs "2.1.4" :scope "test"]
                            [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [adzerk/boot-reload "0.6.0" :scope "test"]
                            [com.cemerick/piggieback "0.2.1" :scope "test"]
                            [binaryage/devtools "0.9.4" :scope "test"]
                            [weasel "0.7.0" :scope "test"]
                            [deraen/boot-sass  "0.3.1" :scope "test"]
                            [etaoin "0.2.8-SNAPSHOT" :scope "test"]
                            [boot-cljfmt "0.1.1" :scope "test"]
                            [tolitius/boot-check "0.1.9" :scope "test"]])


(require '[system.boot :refer [system run]]
         '[app.systems :refer [dev-system]]
         '[clojure.edn :as edn]
         '[deraen.boot-sass :refer [sass]]
         '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]]
         '[boot-cljfmt.core :refer [check fix]]
         '[tolitius.boot-check :as bc])

(require '[adzerk.boot-cljs :refer :all]
         '[adzerk.boot-cljs-repl :refer :all]
         '[adzerk.boot-reload :refer :all])

(task-options!
  aot {:namespace   #{'app.main}}
  jar {:main        'app.main
        :file        (str "app-" version "-standalone.jar")}
  pom {:project project
        :version version}
  repl {:port 6809})  ; This changes the port for the CLJS REPL
                      ; The CLJ REPL is over ridden by the task below

(deftask dev
  "run a restartable system"
  []
  (comp
    (watch :verbose true)
    (system :sys #'dev-system
            :auto true
            :files ["routes.clj" "systems.clj" "api.clj" "query.clj"])
    (repl :server true
          :host "127.0.0.1"
          :port 6502)
    (reload :asset-path "public")
    (cljs-repl)
    (cljs-devtools)
    (cljs :source-map true :optimizations :none)
    (sass)))

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp 
      (aot) 
      (cljs :optimizations :advanced)
      (sass)
      (pom) 
      (uber)
      (jar)
      (target :dir dir))))

(require '[adzerk.boot-test :as bt])

(deftask test 
  "Run tests"
  []
  (comp
    (cljs :optimizations :none)
    (bt/test :include #"app.test")))

(deftask test-watch
  "Automatically re-run tests on file change."
  []
  (comp
    (watch)
    (test)))

(deftask cljfmt
  "Run cljfmt on the src/ directory and fix all formatting issues"
  []
  (comp
    (notify :audible true :visual true)
    (fix :folder "./src/")))

(deftask analyse
  "Run code linting analysis with kibit and bikeshed"
  []
  (comp
    (bc/with-kibit)
    (bc/with-bikeshed)))
