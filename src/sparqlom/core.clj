(ns sparqlom.core
  (:require [sparqlom.query :refer [build-query-element]]
            [sparqlom.spec :refer [parse-query]]
            [clojure.string :refer [join]]))






(defn build-query
  [q]
  (->>(parse-query q)
      (map build-query-element)
      (join " ")))



