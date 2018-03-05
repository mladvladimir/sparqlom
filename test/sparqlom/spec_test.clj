(ns sparqlom.spec-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [sparqlom.spec :as sspec]
            [sparqlom.builder :as sbuilder]
            [clojure.string :as str]
            [sparqlom.utils :as util]))


(def iri-samples ["<www.example.com>" "ex:sample" :ex/sample])

(deftest test-iri-conform
  (testing "test iri conformation fail!!!"
    (is (= (mapv #(s/conform ::sspec/iri %) iri-samples)
           [[:IRIREF "<www.example.com>"]
            [:prefixed-name "ex:sample"]
            [:qualified-prefixed-name :ex/sample]]))))

(def conform-and-build
  (comp
    sbuilder/build-query-element
    s/conform))

(def bracketted-expression-samples ['(= ?a "<www.example>")
                                    '(= ?a "ex:sample")
                                    '(= ?a :ex/sample)])

(deftest test-bracketted-expression-build-query-element
  (testing "test bracketted expression building query element!!!"
    (is (= (map #(conform-and-build
                   :sparqlom.spec/bracketted-expression %)
                bracketted-expression-samples)
           [" ?a  = <www.example>"
            " ?a  = ex:sample"
            " ?a  =  ex:sample "]))))




(deftest test-complex-bracketted-expression-1
  (testing "test bracketted expression building query element!!!"
    (is
      (=
        (-> (conform-and-build
              :sparqlom.spec/conditional-or-expression
              '(and (= (+ ?test (* ?s (- 2))) 100) ?test (= 23 (- 100 ?b))))
            util/normalize-whitespaces)
        "?test + ?s * - 2 = 100 && ?test && 23 = 100 - ?b"))))




;SELECT
(deftest test-select-1
  (testing "test complex select clause 1 !!!"
    (is
      (=
        (->> '[:distinct ?s
               {:expression (and (>= (count ?s :distinct) 100) true)
                               :as ?count}]
             (s/conform ::sspec/select)
             (map sbuilder/build-query-element)
             (flatten)
             (str/join "")
             (util/normalize-whitespaces))
        "DISTINCT ?s (COUNT( DISTINCT ?s ) >= 100 && true AS ?count)"))))


;(conform-and-build
;  :sparqlom.spec/select
;           '[:distinct
;             [?s ?a
;              {:aggregate (/(count ?s) 2) :as ?c}
;              {:aggregate (min ?s) :as ?m}]])
;
;(conform-and-build
;  ::sspec/select
;             '[[?s ?a ?test
;                {:aggregate (count ?s) :as ?count}
;                {:aggregate (sum ?o) :as ?sum}]])
;
;
;
;(conform-and-build
;  ::sspec/select '[:reduced *])
;
;(conform-and-build
;  ::sspec/select '[:reduced :*])



