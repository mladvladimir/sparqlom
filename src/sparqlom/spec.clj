(ns sparqlom.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [sparqlom.parser :as parser]
            [sparqlom.utils :refer :all]))


;namespaces
(s/def ::pn-prefix (s/and keyword?
                       #(parser/prefix-valid? (name %))))

(s/def ::prefix (s/map-of ::pn-prefix ::iri))

(s/def ::prefixed-name (s/and string?
                              #(parser/insta-validate % :PrefixedName)))


;vars
(s/def ::var (s/and symbol?
                    #(parser/var-name-valid? (str %))))


(s/def ::star (s/or :symbol  #(= '* %)
                    :keyword #(= :* %)))


(s/def ::as ::var)

;iri
(s/def ::IRIREF (s/and string?
                       #(parser/iriref-valid? %)))

(s/def ::iri (s/or :IRIREF ::IRIREF
                   :prefixed-name ::prefixed-name
                   :qualified-prefixed-name qualified-keyword?))

;select
;SelectClause	  ::=  	'SELECT' ( 'DISTINCT' | 'REDUCED' )? ( ( Var | ( '(' Expression 'AS' Var ')' ) )+ | '*' )

(s/def ::distinct-or-reduced (s/? #{:distinct :reduced}))

(s/def ::select
  (s/cat :distinct-or-reduced ::distinct-or-reduced
         :vars (s/* ::var)
         :binding-expression (s/* (s/keys :req-un [::expression ::as]))
         :star (s/? ::star)))



;triples
(s/def ::subject (s/or :var ::var
                       :iri ::iri))

(s/def ::predicate (s/or :var ::var
                         :iri ::iri))

(s/def ::object (s/or :var ::var
                      :iri ::iri))

(s/def ::triple (s/tuple ::subject ::predicate ::object))


;where
;s/or is used for vector with key creation - change this!
(s/def ::group-graph-pattern-sub (s/+ (s/or :triple ::triple)))

(s/def ::group-graph-pattern (s/coll-of
                               (s/or :triple ::triple
                                     :group-graph-pattern-sub ::group-graph-pattern-sub
                                     :subselect ::query
                                     :union-graph-pattern ::union-graph-pattern
                                     :optional-graph-pattern ::optional-graph-pattern
                                     :filter-expression ::filter-expression
                                     :filter ::filter
                                     )))

;union
(s/def ::union (s/and ::group-graph-pattern
                      #(<= 2 (count %))))

(s/def ::union-graph-pattern (s/keys :req-un [::union]))

;optional
(s/def ::optional ::group-graph-pattern)

(s/def ::optional-graph-pattern (s/keys :req-un [::optional]))



;filter
(s/def ::filter-expression (s/keys :req-un [::filter]))

(s/def ::where ::group-graph-pattern)



;Aggregate
;Aggregate	  ::=  	  'COUNT' '(' 'DISTINCT'? ( '*' | Expression ) ')'
;    | 'SUM' '(' 'DISTINCT'? Expression ')'
;    | 'MIN' '(' 'DISTINCT'? Expression ')'
;    | 'MAX' '(' 'DISTINCT'? Expression ')'
;    | 'AVG' '(' 'DISTINCT'? Expression ')'
;    | 'SAMPLE' '(' 'DISTINCT'? Expression ')'
;    | 'GROUP_CONCAT' '(' 'DISTINCT'? Expression ( ';' 'SEPARATOR' '=' String )? ')'

(def aggregate-functions '#{count sum min max avg sample group-concat})
(s/def ::aggregate (s/cat :name aggregate-functions
                          :expression ::expression
                          :is-distinct (s/? #{:distinct})))



;Literals

;filter

;ArgList	  ::=  	NIL | '(' 'DISTINCT'? Expression ( ',' Expression )* ')'



;Expressions



(s/def ::arg-list (s/or :nil nil?
                        :modifier (s/? #{:distinct})
                        :expression (s/* ::expression)
                        ))

(s/def ::iri-or-function (s/or :iri  ::iri
                               :arg-list (s/? ::arg-list)))

(s/def ::builtin-call ::aggregate)

(def relational-operators '#{= != < > <= >= in not-in})
(def additive-operators '#{+ -})
(def multiplicative-operators '#{* /})
(def unary-operators '#{! + -} )

(s/def ::unary-expression
  (s/or :unary-expression
        (s/cat :operator  unary-operators
               :operands  ::primary-expression)
        :primary-expression ::primary-expression))

(s/def ::multiplicative-expression
  (s/or :multiplicative-expression
        (s/cat :operator  multiplicative-operators
               :operands   (s/+ (s/and ::unary-expression
                                       #(>= (count %) 2))))
        :unary-expression ::unary-expression))


(s/def ::additive-expression
  (s/or :additive-expression
        (s/cat :operator  additive-operators
               :operands (s/+ ::multiplicative-expression))
        :multiplicative-expression ::multiplicative-expression))


(s/def ::numeric-expression ::additive-expression)

(s/def ::relational-expression
  (s/or :relational-expression
        (s/cat :operator relational-operators
               :operands (s/+ (s/and ::numeric-expression
                                     #(>= (count %) 2))))
        :numeric-expression ::numeric-expression))


(s/def ::value-logical ::relational-expression)

(s/def ::conditional-and-expression
  (s/or :conditional-and-expression
        (s/cat :operator '#{and}
               :operands (s/+ ::value-logical))
        :value-logical ::value-logical))

(s/def ::conditional-or-expression
  (s/or :conditional-or-expression
        (s/cat :operator '#{or}
               :operands (s/+ ::conditional-and-expression))
        :conditional-and-expression ::conditional-and-expression))

(s/def ::expression ::conditional-or-expression)


(s/def ::bracketted-expression ::expression)

(s/def ::primary-expression
  (s/or :numeric-literal number?
        :boolean-literal boolean?
        :var ::var
        :builtin-call ::builtin-call
        :iri-or-function ::iri-or-function
        :rdf-literal #(parser/rdf-literal-valid? (str %))
        :bracketted-expression ::bracketted-expression))

;Constraint	  ::=  	BrackettedExpression | BuiltInCall | FunctionCall
(s/def ::constraint  (s/or :bracketted-expression ::bracketted-expression
                           :builtin-call ::builtin-call))

(s/def ::filter ::constraint)



;modifiers
(s/def ::limit pos-int?)
(s/def ::offset pos-int?)
(s/def ::order-modifier #{:asc :desc})


(s/def ::order-by (s/*
                   (s/cat :var ::var
                          :order-modifier (s/? ::order-modifier))))


;groupby
;GroupCondition	  ::=  	BuiltInCall | FunctionCall | '(' Expression ( 'AS' Var )? ')' | Var

(s/def ::group-by (s/+ (s/or :builtin-call ::builtin-call
                             :expression ::expression
                             :var ::var)))

;query
(s/def ::query (s/keys :req-un [::select ::where]
                       :opt-un [::prefix ::limit ::offset ::order-by]))



;SolutionModifier	  ::=  	GroupClause? HavingClause? OrderClause? LimitOffsetClauses?
;(s/def ::solution-modifier
;  (s/cat :group-clause
;         :order-clause
;         :limit-offset-clause))




;Construct
;<GraphTerm>	  ::=  	iri |	RDFLiteral |	NumericLiteral |	BooleanLiteral |	BlankNode |	NIL
(s/def ::graph-term  (s/or :iri ::iri
                           :rdf-literal #(parser/rdf-literal-valid? (str %))
                           :numeric-literal number?
                           :boolean-literal boolean?
                           ;:blank-node ::blank-node
                           :nil nil?))

;<VarOrTerm>	  ::=  	Var | GraphTerm
(s/def ::var-or-term (s/or :var ::var
                           :graph-term ::graph-term))
;VarOrIri	  ::=  	Var | iri
(s/def ::var-or-iri (s/or :var ::var
                          :iri ::iri))

;;GraphNode	  ::=  	VarOrTerm |	TriplesNode
(s/def ::graph-node
  (s/or :var-or-term ::var-or-term
        ;:triples-node ::triples-node
        ))

;Object	  ::=  	GraphNode
(s/def ::object ::graph-node)

;ObjectList	  ::=  	Object ( ',' Object )*
(s/def ::object-list (s/+ ::object))

;Verb	  ::=  	VarOrIri | 'a'
(s/def ::verb (s/or :var-or-iri ::var-or-iri
                    :a (s/or :symbol  #(= 'a %)
                             :keyword #(= :a %))))

;PropertyListNotEmpty	  ::=  	Verb ObjectList ( ';' ( Verb ObjectList )? )*
(s/def ::property-list-not-empty
  (s/+
    (s/cat :verb ::verb
           :object (s/or :object ::object
                         :object-list ::object-list))))


;
;;TriplesNode	  ::=  	Collection |	BlankNodePropertyList
;(s/def ::triples-node
;  (s/or :collection (s/+ ::graph-node)
;        ;:blank-node-property-list ::blank-node-property-list
;        ))

;TriplesSameSubject	  ::=  	VarOrTerm PropertyListNotEmpty |	TriplesNode PropertyList
(s/def ::triples-same-subject
  (s/cat :var-or-term ::var-or-term
         :property-list-not-empty ::property-list-not-empty))

;ConstructTriples	  ::=  	TriplesSameSubject ( '.' ConstructTriples? )?
(s/def ::construct-triples
  (s/cat :triples-same-subject ::triples-same-subject
         :construct-triples (s/? ::construct-triples)))

;ConstructTemplate	  ::=  	'{' ConstructTriples? '}'
(s/def ::construct-template (s/? ::construct-triples))
(s/def ::construct ::construct-template)

;ConstructQuery	  ::=  	'CONSTRUCT' ( ConstructTemplate DatasetClause* WhereClause SolutionModifier | DatasetClause* 'WHERE' '{' TriplesTemplate? '}' SolutionModifier )
(s/def ::constuct-query
  (s/keys :req-un [::construct ::where ::solution-modifier]
          :opt-un [::data-clause]))



(defn parse-query
  [q]
  (let [parsed-query (s/conform ::query q)]
    (if (s/invalid? parsed-query)
      (throw
        (ex-info "Invalid query format" (s/explain ::query q)))
      parsed-query)))





