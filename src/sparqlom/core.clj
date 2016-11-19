(ns sparqlom.core
  (:require [sparqlom.helper :refer [build-query-element]]
            [sparqlom.spec :refer [parse-query]]
            [clojure.string :refer [join]]))






(defn ->sparql
  [q]
  (->>(parse-query q)
      (map build-query-element)
      (join " ")))



