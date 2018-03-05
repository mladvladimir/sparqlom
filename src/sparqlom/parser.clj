(ns sparqlom.parser
  (:require [instaparse.core :as insta]
            [sparqlom.utils :refer [remove-whitespace]]))


(def grammar "sparql.ebnf")

;Parsers
(def sparql-parser
  (insta/parser (clojure.java.io/resource grammar)))

(defn partial-parser
  [s tag]
  (insta/parse
    sparql-parser
    (remove-whitespace s)
    :start tag))

(defn parse-prologue
  [s]
  (partial-parser s :Prologue))

(defn parse-select
  [s]
  (partial-parser s :SelectClause))

(defn parse-limit-offset
  [s]
  (partial-parser s :LimitOffsetClauses))

(defn parse-triple
  [s]
  (partial-parser s :TriplesBlock))







;SPARQL string parser
(defn parse-element
  [s tag]
  (sparql-parser
    (remove-whitespace s)
    :start tag))

;SPARQL string validator
(defn insta-validate
  [s tag]
  (->(parse-element s tag)
     (insta/failure?)
     (not)))



(defn prologue-valid?
  [prolog]
  (insta-validate prolog :Prologue))

(defn prefix-declaradion-valid?
  [prefix-decl]
  (insta-validate prefix-decl :PrefixDecl))

(defn prefix-valid?
  [prefix]
  (insta-validate prefix :PN_PREFIX))


(defn var-name-valid?
  [var-name]
  (insta-validate var-name :Var))


(defn iriref-valid?
  [iri-string]
  (insta-validate iri-string :IRIREF))

(defn rdf-literal-valid?
  [rdf-literal]
  (insta-validate rdf-literal :RDFLiteral))

(defn select-valid?
  [clause]
  (insta-validate clause :SelectClause))
