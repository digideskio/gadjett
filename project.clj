(defproject viebel/gadjett "0.1.6"
  :description "Inspector tools for clojurescript"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [im.chit/purnam "0.5.2"]]
  
  :plugins [[lein-cljsbuild "1.1.2"]]
  :source-paths ["src"]
  :cljsbuild {
              :builds
              {
               :dev {
                :source-paths ["src"]
                :compiler {
                           :output-to "public/clojurescript/dev/main.js"
                           :optimizations :simple
                           :pretty-print true}}}})
