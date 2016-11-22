(ns sparqlom.core-test
  (:require [clojure.test :refer :all]
            [sparqlom.core :refer :all]))



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


(def query-3 '{:select [?s ?p ?o :distinct]
               :where [[?s ?p ?o]
                       {:filter (and
                                  (>= ?o 100)
                                  (< ?o 123))}]
               :limit 100})

(def query-3-sparql "SELECT DISTINCT ?s ?p ?o WHERE {?s ?p ?o . FILTER ((?o >= 100) && (?o < 123))} LIMIT 100")

(deftest test-query-3
  (testing "test query-2 fail!!!"
    (is (= (->sparql query-3) query-3-sparql))))

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
                                :where [["<http:example.com>" ?p ?s]
                                        [?s  :rdf/type  :owl/Thing]
                                        {:optional [["<http:example.com>" ?b ?s]]}]}]}]
                     :limit 100
                     :offset 100})


(def complex-query
  '{:prefix {:owl "<http://www.w3.org/2002/07/owl#>"
             :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
             :rdf "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"}
    :select [?s :distinct]
    :where [[?s :rdf/type :owl/Thing]
            {:union
             [[?s ?a ?b]
              {:select [* :distinct]
               :where [["<http:example.com>" ?p ?s]
                       [?s  :rdf/type  :owl/Thing]]}]}]
    :limit 100
    :offset 100})



(def optional-query '{:select [*]
                      :where [[?s ?p ?o]
                              {:optional [[?a ?b ?c]
                                          [?A ?B ?C]]}]})

(def optional-query-sparql "SELECT  * WHERE {?s ?p ?o . OPTIONAL {?a ?b ?c . ?A ?B ?C .}}")

(deftest test-optional-query
  (testing "test optional-query fail!!!"
    (is (= (->sparql optional-query) optional-query-sparql))))

;defprefix
;(defprefix :owl "<http://www.w3.org/2002/07/owl#>"
;           :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
;           :ex "<http://www.example.com>")


(def prefix-string "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>")

(def select-string-1 "SELECT *")
(def select-string-2 "SELECT DISTINCT * ")
(def select-string-3 "SELECT REDUCED ?s ?p ?o")

(def limit-offset "OFFSET 10 LIMIT 10")
