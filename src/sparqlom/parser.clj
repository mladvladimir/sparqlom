(ns sparqlom.parser
  (:require [instaparse.core :as insta]))




(def sparql-parser (insta/parser "./resources/sparql.ebnf" ))

(defn insta-validate
  [element tag]
  (->(sparql-parser element :start tag)
     (insta/failure?)
     (not)))

(defn prefix-valid?
  [prefix]
  (insta-validate prefix :PN_PREFIX))


(defn var-name-valid?
  [var-name]
  (insta-validate var-name :VAR1))


(defn iri-valid?
  [iri]
  (insta-validate iri :IRIREF))
