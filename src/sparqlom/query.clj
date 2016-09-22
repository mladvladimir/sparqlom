(ns sparqlom.query
  (:require [clojure.string :refer [join]])
  (:use sparqlom.spec))




(defn select-projection
  [parsed-select]
  (cond->> (val parsed-select)
           (= :vars (key parsed-select)) (join " " )
           (= :star (key parsed-select)) (str)))




(defmulti build-query-element (fn [element] (key element)))


(defmethod build-query-element :prefix
  [element]
  (->>(val element)
      (map #(join "" ["PREFIX " (name ( key %)) ": " (val %)]))
      (join " " )))


(defmethod build-query-element :select
  [element]
  (let [parsed-select (:vars-or-star (val element))
        distinct-or-reduced ({:distinct "DISTINCT"
                              :reduced "REDUCED"}
                              (:distinct-or-reduced (val element)))]
    (join " " ["SELECT"
               distinct-or-reduced
               (select-projection parsed-select)])))


(defmethod build-query-element :where
  [element]
  (str "WHERE {"
       (->>(val element)
           (map build-query-element)
           (join " "))
       "}"))


;(defmethod build-query-element :triple
;  [element]
;  (let [[[_ s][_ p][_ o]] (val element)]
;    (join " " [ s p o "."])))

(defmethod build-query-element :triple
  [element]
  (str
    (->>(val element)
        (map build-query-element)
        (join " "))
    " ."))

(defmethod build-query-element :var
  [element]
  (val element))


(defmethod build-query-element :iri
  [element]
  (val element))

(defmethod build-query-element :prefixed-name
  [element]
  (->>(val element)
      ((juxt namespace name))
      (join ":")))


(defmethod build-query-element :group-graph-pattern-sub
  [element]
  (str "{"
       (->>(val element)
           (map build-query-element)
           (join " "))
       "}"))


(defmethod build-query-element :subselect
  [element]
  (str "{"
       (->>(val element)
           (map build-query-element)
           (join " "))
       "}"))

(defmethod build-query-element :union-graph-pattern
  [element]
  (->>(val element)
      (:union)
      ;(map build-query-element)
      (map #(if (= :triple (key %))
             (str "{" (build-query-element %) "}")
             (build-query-element %)))
      (join " UNION ")))


(defmethod build-query-element :limit
  [element]
  (str "LIMIT " (val element)))

(defmethod build-query-element :offset
  [element]
  (str "OFFSET " (val element)))





















