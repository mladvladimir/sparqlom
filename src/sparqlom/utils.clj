(ns sparqlom.utils
  (:require [clojure.string :as str]))


(defn is-prefixed?
  [prefixed]
  (->(namespace prefixed)
     (nil?)
     (not)))

(defn find-prefixed
  [q]
  (->> (tree-seq #(or (map? %) (vector? %)) seq  q)
       (filter (every-pred keyword? is-prefixed?))))


(defn remove-whitespace
  [words]
  (str/join "" (str/split words #"\s+")))

(defn space-enclose
  [element]
  (str " " element " "))

(defn parentheses-enclose [element] (str "(" element ")"))

(defn braces-enclose
  [f element]
  (str "{"
       (->>(val element)
           (map f)
           (str/join " "))
       "}"))

(defn normalize-whitespaces
  [s]
  (-> s
      (clojure.string/replace  #"\s\s+" " ")
      str/triml
      str/trimr))

