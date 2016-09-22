(ns sparqlom.core-test
  (:require [clojure.test :refer :all]
            [sparqlom.core :refer :all]))

;(deftest a-test
;  (testing "FIXME, I fail."
;    (is (= 0 1))))


(def test-query '{:select [* :distinct]
                  :where [[[?s ?p ?o]
                           [?a ?b ?c]]
                          [?A ?b ?c]
                          {:select [?A ?B]
                           :where [[?A ?B ?C]]}
                          {:select [* :distinct]
                           :where [["<http:example.com>" ?p ?o]]}]})


(def test-query-1 '{:select [* :distinct]
                    :where [[[?s ?p ?o]
                             [?a ?b ?c]]
                            [?A ?b ?c]
                            {:select [?A ?C]
                             :where [[?A :rdf/type ?C]]}
                            {:select [* :distinct]
                             :where [["<http:example.com>" ?p ?o]]}]})



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


(def test-query-union '{:prefix {:owl "<http://www.w3.org/2002/07/owl#>"
                                 :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"}
                        :select [?A ?B ?C]
                        :where [[?A ?B ?C]
                                {:union [[?s ?p ?o]
                                         [[?s ?a ?b]
                                          [?o ?p ?q]]]}]
                        })
