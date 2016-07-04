(defproject pixel-blobs "0.1.0-SNAPSHOT"
  :description "Pixel blobs in HTML canvas"
  :url "http://bitantics.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [quil "2.4.0"]
                 [org.clojure/clojurescript "1.9.89"]]

  :plugins [[lein-cljsbuild "1.1.3"]]
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds [{:source-paths ["src"]
             :compiler
             {:output-to "js/main.js"
              :output-dir "out"
              :main "pixel_blobs.core"
              :optimizations :none
              :pretty-print true}}]})
