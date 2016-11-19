# SPARQLom

SPARQLom is SPARQL query builder and DSL for clojure based on clojure.spec and instaparse. It's in early development phase.

## Usage

Represent SPARQL query as a map: 

```clj

(def query '{:select [?s ?p ?o :distinct] 
             :where [[?s ?p ?o]
                     {:filter (and 
                                (>= ?o 100) 
                                (< ?o 123))}]
             :limit 100})
```

Call `->sparql` method to get SPARQL query string:

```clj

(require [sparqlom.core :refer [->sparql]])
(->sparql query)
=> "SELECT DISTINCT ?s ?p ?o WHERE {?s ?p ?o . FILTER ((?o >= 100) && (?o < 123))} LIMIT 100"
```

More complex example:

```clojure

(def complex-query 
                   '{:prefix {:owl "<http://www.w3.org/2002/07/owl#>"
                              :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
                              :rdf "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"}
                     :select [?s :distinct]
                     :where [[?s :rdf/type :owl/Thing]
                             {:union
                             [[?s ?a ?b]
                              {:select [* :distinct]
                               :where [["<http:example.com>" ?p ?s]
                                       [?s  :rdf/type  :owl/Thing]]}]}]
                     :limit 100
                     :offset 100})
                    
(->sparql complex-query)
=>"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?s WHERE {?s rdf:type owl:Thing . {?s ?a ?b .} UNION {SELECT DISTINCT * WHERE {<http:example.com> ?p ?s . ?s rdf:type owl:Thing .}}} LIMIT 100 OFFSET 100"
```


## TODO
 
- OPTIONAL
- FILTER functions
- SELECT expressions
- CONDTRUCT, ASK
- property paths 
- more query validations
- transformation from SPARQL to SPARQLom map
- SPARQL pretty printer 
- ...

## Feedback and contributions are welcome !!!

## License

Copyright © 2016 Vladimir Mladenovic

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
# sparqlom
