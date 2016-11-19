(ns sparqlom.utils
  (:require [clojure.string :refer [split join]]))


(defn is-prefixed?
  [prefixed]
  (->(namespace prefixed)
     (nil?)
     (not)))

(defn find-prefixed
  [q]
  (->> (tree-seq #(or (map? %) (vector? %)) seq  q)
       (filter (every-pred keyword? is-prefixed?))))

;(defn build-iri
;  [prefixed-name]
;  (namespace prefixed-name))

(defn remove-whitespace
  [words]
  (join "" (split words #"\s+")))