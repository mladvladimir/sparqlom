(ns sparqlom.query-elements
  (:require [clojure.test :refer :all]
            [sparqlom.core :refer :all]
            [sparqlom.query :refer [defprefix]]))


;prefixes
(def prefix-string "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>")

;(defprefix :rdf )

;(defprefix :owl "<http://www.w3.org/2002/07/owl#>"
;           :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
;           :ex "<http://www.example.com>")