(ns sparqlom.builder
  (:require [clojure.string :refer [join
                                    lower-case]]
            [clojure.spec.alpha :as s])
  (:require [sparqlom.spec :as sspec]
            [clojure.string :as str]
            [sparqlom.utils :as util]))



(defmulti build-query-element (fn [element] (key element)))

(defmethod build-query-element :prefix
  [element]
  (->> (val element)
       (map #(str/join "" ["PREFIX " (name ( key %)) ": " (->> (val %)
                                                               build-query-element)]))
       (str/join " ")))

(defmethod build-query-element :select
  [element]
  (str "SELECT "
       (->> (val element)
            (map build-query-element)
            ;((fn [x] (println "posle mape" x) x))
            flatten
            str/join)))


;(defmethod build-query-element :construct-triples)


(defmethod build-query-element :where
  [element]
  (str "WHERE "
       (util/braces-enclose build-query-element element)))

(defmethod build-query-element :triple
  [element]
  (str
    (->> (val element)
         (map build-query-element)
         (str/join ""))
    "."))

(defmethod build-query-element :group-graph-pattern-sub
  [element]
  (util/braces-enclose build-query-element element))


(defmethod build-query-element :subselect
  [element]
  (util/braces-enclose build-query-element element))

(defmethod build-query-element :union-graph-pattern
  [element]
  (->> (val element)
       (:union)
       (map #(if (= :triple (key %))
              (str "{" (build-query-element %) "}")
              (build-query-element %)))
       (str/join " UNION ")))

(defmethod build-query-element :optional-graph-pattern
  [element]
  (str "OPTIONAL "
       (->> (val element)
            (first)
            (util/braces-enclose build-query-element)
            )))


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
       (->> (val element)
            :filter
            build-query-element)
       ")"))


(defmethod build-query-element :binding-expression
  [element]
  (->> (val element)
       (map  #(map build-query-element %))
       (map #(util/parentheses-enclose (str/join " AS " %)))
       (flatten)))


(defmethod build-query-element :var
  [element]
  (util/space-enclose (val element)))

(defmethod build-query-element :vars
  [element]
  (util/space-enclose
    (->> (val element)
         (#(if (vector? %) (str/join " " %) %)))))

(defmethod build-query-element :as
  [element]
  (->> (val element)))



(defmethod build-query-element :qualified-prefixed-name
  [element]
  (util/space-enclose
    (->> (val element)
         ((juxt namespace name))
         (str/join ":"))))



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


(defn cat-ops
  [op-expression]
  (let [operator (:operator  op-expression)
        operands (:operands  op-expression)
        operator-str (operator operators)]
    (cond
      (map-entry? operands) (str operator-str (build-query-element operands))
      (vector? operands) (str/join operator-str (map build-query-element operands)))))


(defmethod build-query-element :conditional-or-expression
  [element]
  (->> element
       val
       cat-ops))


(defmethod build-query-element :builtin-call
  [element]
  (let [[name expression is-distinct] (map build-query-element (val element))]
    (str name "(" is-distinct  expression ")")))


(defmethod build-query-element :default
  [element]
  (let [expression-val (val element)]
    (cond
      (map-entry? expression-val) (build-query-element expression-val)
      (vector?    expression-val) (map build-query-element expression-val)
      (map?       expression-val) (cat-ops expression-val)
      (keyword?   expression-val) (util/space-enclose
                                    (str/upper-case (name expression-val)))
      (symbol?    expression-val) (str/upper-case expression-val)
      (number?    expression-val) (str expression-val)
      (boolean?   expression-val) (str expression-val)
      :else expression-val)))













;Buillding sparqlom query map from parsed string
;currently under development

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



