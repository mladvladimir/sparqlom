(defproject sparqlom "0.1.0-SNAPSHOT"
  :description "SPARQLom - SPARQL query builder and dsl"
  :url "https://github.com/mladvladimir/sparqlom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :profile {:1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
            :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
            :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [instaparse "1.4.5"]])

  :resource-paths ["resources"]

  :scm {:name "git"
        :url "https://github.com/mladvladimir/sparqlom"}