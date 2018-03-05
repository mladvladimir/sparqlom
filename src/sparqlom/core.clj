(ns sparqlom.core
  (:require [sparqlom.builder :refer [build-query-element]]
            [sparqlom.spec :refer [parse-query]]
            [clojure.string :as str]
            [sparqlom.utils :as util]))






(defn ->sparql
  [q]
  (->> (parse-query q)
       (map build-query-element)
       (str/join " ")
       (util/normalize-whitespaces)))


