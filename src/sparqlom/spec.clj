(ns sparqlom.spec
  (:require [clojure.string :as str]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [sparqlom.parser :refer :all]
            [sparqlom.utils :refer :all]))


;namespaces
(s/def ::pn-prefix (s/and keyword?
                       #(prefix-valid? (name %))))

(s/def ::prefix (s/map-of ::pn-prefix ::iri))

(s/def ::prefixed-name (s/and keyword?
                              #(not (nil? (namespace %)))))


;vars
(s/def ::var (s/and symbol?
                    #(var-name-valid? (str %))))

(s/def ::vars (s/+ ::var))

(s/def ::star (s/and symbol?
                     #(= '* %)))

;(s/def ::vars (s/coll-of ::var :kind vector? :min-count 1 ) )
;(s/def ::var-or-expression )

(s/def ::vars-or-star (s/alt :vars ::vars
                             :star ::star))


;IRI
(s/def ::iri #(iri-valid? (str %)))


;select
(s/def ::distinct-or-reduced (s/? #{:distinct :reduced}))

(s/def ::select-type (s/cat :vars-or-star ::vars-or-star
                            :distinct-or-reduced ::distinct-or-reduced ))


(s/def ::select ::select-type)


;triples
(s/def ::subject (s/or :var ::var
                       :prefixed-name ::prefixed-name
                       :iri ::iri))

(s/def ::predicate (s/or :var ::var
                         :prefixed-name ::prefixed-name
                         :iri ::iri))

(s/def ::object (s/or :var ::var
                      :prefixed-name ::prefixed-name
                      :iri ::iri))

;(s/def ::triple (s/cat :subject ::subject
;                       :predicate ::predicate
;                       :object ::object))

(s/def ::triple (s/tuple ::subject ::predicate ::object))


;where
;s/or is used for vector with key creation - change this!
(s/def ::group-graph-pattern-sub (s/+ (s/or :triple ::triple)))

(s/def ::group-graph-pattern (s/coll-of
                               (s/or :triple ::triple
                                     :group-graph-pattern-sub ::group-graph-pattern-sub
                                     :subselect ::query
                                     :union-graph-pattern ::union-graph-pattern
                                     :filter-expression ::filter-expression
                                     )))

;union
(s/def ::union (s/and ::group-graph-pattern
                      #(<= 2 (count %))))

(s/def ::union-graph-pattern (s/keys :req-un [::union]))



(s/def ::filter-expression (s/keys :req-un [::filter]))


(s/def ::where ::group-graph-pattern)



;Literals


;filter
;Constraint	  ::=  	BrackettedExpression | BuiltInCall | FunctionCall



(def relational-operators '#{= != < > <= >= in not-in})
(def additive-operators '#{+ -})
(def multiplicative-operators '#{* /})
(def unary-operators '#{! + -} )

(s/def ::primary-expression
  (s/or
        :bracketted-expression ::bracketted-expression
        ;:builtin-call ::builtin-call
        ;:iri-or-function ::iri-or-function
        :rdf-literal #(rdf-literal-valid? (str %))
        :numeric-literal number?
        :boolean-literal boolean?
        :var ::var
        ))

(s/def ::unary-expression
  (s/cat
    :operator unary-operators
    :operands ::primary-expression))

(s/def ::multiplicative-expression
  (s/cat
    :operator multiplicative-operators
    :operands (s/+ ::primary-expression)))

(s/def ::additive-expression
  (s/cat
    :operator additive-operators
    :operands (s/+ ::primary-expression)))


(s/def ::relational-expression
  (s/cat
    :operator relational-operators
    :operands (s/&
                (s/+ ::primary-expression)
                #(-> % count (= 2)))))


(s/def ::and-expression
  (s/cat
    :operator '#{and}
    :operands (s/+ ::primary-expression)))

(s/def ::or-expression
  (s/cat
    :operator '#{or}
    :operands (s/+ ::primary-expression)))


(s/def ::bracketted-expression
  (s/or
    :or-expression ::or-expression
    :and-expression ::and-expression
    :relational-expression ::relational-expression
    :additive-expression ::additive-expression
    :multiplicative-expression ::multiplicative-expression
    :unary-expression ::unary-expression))


(s/def ::constraint  ::bracketted-expression)

(s/def ::filter ::constraint)




;modifiers
;limit
(s/def ::limit integer?)
;offset
(s/def ::offset integer?)
;orderby
(s/def ::order-modifier #{:asc :desc})


(s/def ::orderby (s/*
                   (s/cat :var ::var
                          :order-modifier (s/? ::order-modifier))))


;groupby
(s/def ::groupby ::vars)


;(s/def ::modifiers (s/keys :opt-un [::limit ::offset ::orderby]))

;query
(s/def ::query (s/keys :req-un [::select ::where]
                       :opt-un [::prefix ::limit ::offset ::orderby]))


(defn parse-query
  [q]
  (let [parsed-query (s/conform ::query q)]
    (if (s/invalid? parsed-query)
      (throw
        (ex-info "Invalid query format" (s/explain ::query q)))
      parsed-query)))



;
;(s/fdef defselect
;        :args ::select)

;(defn parse-filter
;  [filter-expression]
;  (let [filter (s/conform ::constraint filter-expression)]
;    (if s/invalid?)))



