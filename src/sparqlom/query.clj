(ns sparqlom.query
  (:require [sparqlom.spec :as ss]
            [clojure.spec :as s]
            ;[clojure.spec.test :as test]
            ;[clojure.spec.test :as stest]
            [sparqlom.helper :refer :all]
            [sparqlom.utils :refer :all]
            [sparqlom.parser :refer :all]))


(defn ->prefix-map
  [s]
  (->(parse-prologue s)
     (build-query-map)))

(defn ->prefix-sparql
  [sparqlom]
  (->(build-query-element)))

(defn ->select-map
  [s]
  (->(parse-select s)
     (build-query-map)))

(defn ->limit-offset-map
  [s]
  (->(parse-limit-offset s)
     (build-query-map)))

(defn ->triple
  [s]
  (->(parse-triple s)
     (build-query-map)))

(defn defprefix
  [& declaration]
  {:pre [(s/valid? (s/+ (s/cat :pn-prefix ::ss/pn-prefix :iri ::ss/iri)) declaration)]
   :post [(s/valid? #(prologue-valid? (remove-whitespace %)) %)]}
  (->>(apply hash-map declaration)
      (assoc-in {} [:prefix])
      (apply build-query-element)))

;(defn defselect
;  [& [vars distinct-or-reduced ]])

;(defn deftriple

;(defn defselect
;  [clause]
;  (->>build )
;  (s/conform ::select clause))
;