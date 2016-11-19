(ns sparqlom.core-test
  (:require [clojure.test :refer :all]
            [sparqlom.core :refer :all]))

;(deftest a-test
;  (testing "FIXME, I fail."
;    (is (= 0 1))))


(def query-1 '{:select [* :distinct]
                  :where [[[?s ?p ?o]
                           [?a ?b ?c]]
                          [?A ?b ?c]
                          {:select [?A ?B]
                           :where [[?A ?B ?C]]}
                          {:select [* :distinct]
                           :where [["<http:example.com>" ?p ?o]]}]})

(def query-1-sparql
  "SELECT DISTINCT * WHERE {{?s ?p ?o . ?a ?b ?c .} ?A ?b ?c . {SELECT  ?A ?B WHERE {?A ?B ?C .}} {SELECT DISTINCT * WHERE {<http:example.com> ?p ?o .}}}")

(deftest test-query-1
  (testing "test query-1 fail!!!"
    (is (= (->sparql query-1) query-1-sparql))))


(def query-2 '{:select [* :distinct]
               :where [[[?s ?p ?o]
                        [?a ?b ?c]]
                       [?A ?b ?c]
                       {:select [?A ?C]
                        :where [[?A :rdf/type ?C]]}
                       {:select [* :distinct]
                        :where [["<http:example.com>" ?p ?o]]}]})

(def query-2-sparql
  "SELECT DISTINCT * WHERE {{?s ?p ?o . ?a ?b ?c .} ?A ?b ?c . {SELECT  ?A ?C WHERE {?A rdf:type ?C .}} {SELECT DISTINCT * WHERE {<http:example.com> ?p ?o .}}}")

(deftest test-query-2
  (testing "test query-2 fail!!!"
    (is (= (->sparql query-2) query-2-sparql))))


(def union-query '{:prefix {:owl "<http://www.w3.org/2002/07/owl#>"
                            :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"}
                   :select [?A ?B ?C]
                   :where [[?A ?B ?C]
                           {:union [[?s ?p ?o]
                                    [[?s ?a ?b]
                                     [?o ?p ?q]]]}]})


(def union-query-sparql
  "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT  ?A ?B ?C WHERE {?A ?B ?C . {?s ?p ?o .} UNION {?s ?a ?b . ?o ?p ?q .}}")

(deftest test-union-query
  (testing "test union-query fail!!!"
    (is (= (->sparql union-query) union-query-sparql))))


(def complex-query '{:prefix {:owl "<http://www.w3.org/2002/07/owl#>"
                              :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
                              :rdf "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"}
                     :select [?s :distinct]
                     :where [[?s :rdf/type :owl/Thing]
                             {:union
                              [[?s ?a ?b]
                               {:select [* :distinct]
                                :where [["<http:example.com>" ?p ?s]]}]}]
                     :limit 100
                     :offset 100})


;defprefix
;(defprefix :owl "<http://www.w3.org/2002/07/owl#>"
;           :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
;           :ex "<http://www.example.com>")


(def prefix-string "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>")

(def select-string-1 "SELECT *")
(def select-string-2 "SELECT DISTINCT * ")
(def select-string-3 "SELECT REDUCED ?s ?p ?o")

(def limit-offset "OFFSET 10 LIMIT 10")
