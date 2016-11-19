(ns sparqlom.helper
  (:require [clojure.string :refer [join
                                    lower-case]])
  (:use sparqlom.spec))




(defn select-projection
  [parsed-select]
  (cond->> (val parsed-select)
           (= :vars (key parsed-select)) (join " " )
           (= :star (key parsed-select)) (str)))

(def operators
  '{or " || "
    and " && "
    = " = "
    != " != "
    < " < "
    > " > "
    <= " <= "
    >= " >= "
    in " in "
    not-in " not-in "
    + " + "
    - " - "
    * " * "
    / " / "})



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

(defmethod build-query-element :numeric-literal
  [element]
  (val element))

(defmethod build-query-element :boolean-literal
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

;Building filter expressions
(defmethod build-query-element :filter-expression
  [element]
  (str "FILTER ("
       (->>(val element)
           (:filter)
           (build-query-element))
       ")"))

(defmethod build-query-element :bracketted-expression
  [expression]
  (str "("
       (->>(val expression)
           (build-query-element)
           (join ""))
       ")"))

(defn- cat-ops
  [expression]
  (let [operator (:operator (val expression))
        operands (:operands (val expression))]
    (join
      (operator operators)
      (map build-query-element operands))))

(defmethod build-query-element :or-expression
  [expression]
  (cat-ops expression))

(defmethod build-query-element :and-expression
  [expression]
  (cat-ops expression))

(defmethod build-query-element :relational-expression
  [expression]
  (cat-ops expression))

(defmethod build-query-element :additive-expression
  [expression]
  (cat-ops expression))

(defmethod build-query-element :multiplicative-expression
  [expression]
  (cat-ops expression))








;Buillding sparqlom query map from parsed string
(defmulti build-query-map (fn [s] (nth s 0)))

(defmethod build-query-map :PN_PREFIX
  [s]
  (->>(rest s)
      (join)
      (keyword)))

(defmethod build-query-map :IRIREF
  [s]
  (->>(rest s)
      (join)))

(defmethod build-query-map :PNAME_NS
  [s]
  (->>(second s)
      (build-query-map)))

(defmethod build-query-map :PrefixDecl
  [s]
  (->>(nthnext s 2)
      (map build-query-map)
      (apply hash-map)))

(defmethod build-query-map :Prologue
  [s]
  (->>(rest s)
      (map build-query-map)
      (into {})
      (assoc-in {} [:prefix])))

;check if *
(defn- build-proj
  [proj]
  (if (some #{"*"} proj)
    (conj [] '*)
    (->>(filter vector? proj)
        (map build-query-map)
        (vec))))

(defmethod build-query-map :SelectClause
  [s]
  (assoc-in {} [:select]
            (if-let [modifier (some #{"DISTINCT" "REDUCED"} s)]
              (->>(lower-case modifier)
                 (keyword)
                 (conj (build-proj s)))
              (build-proj s))))


(defmethod build-query-map :Var
  [s]
  (let [prefix  (second s)]
  (->>(last s)
      (build-query-map)
      (str prefix)
      (symbol))))

(defmethod build-query-map :VARNAME
  [s]
  (->>(rest s)
      (join)))

;(defmethod build-query-map :PrefixedName
;  [s]
;  (->>(rest s)
;      (join build-query-map)))

(defmethod build-query-map :PNAME_LN
  [s]
  (->>(rest s)
      (map build-query-map)
      (keyword)))

(defmethod build-query-map :LimitOffsetClauses
  [s]
  (->>(rest s)
      (map build-query-map)
      (into {})))

(defmethod build-query-map :OffsetClause
  [s]
  (assoc-in {} [:offset]
            (build-query-map
              (last s))))

(defmethod build-query-map :LimitClause
  [s]
  (assoc-in {} [:limit]
            (build-query-map
              (last s))))

(defmethod build-query-map :INTEGER
  [s]
  (->>(rest s)
      (apply read-string)))

(defmethod build-query-map :TriplesBlock
  [s]
  (->>(rest s)
      (map build-query-map)
      (vec)))