(ns goodreads.config.interceptors
  (:require [environ.core :refer [env]]
            [clojure.data.xml :refer :all]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as xz]))

(defn zip-body [res]
  (-> res :body
      java.io.StringReader.
      parse
      z/xml-zip))

(defn map-book-id-to-book [book-loc]
  {(xz/xml1-> book-loc :id xz/text)
   {:rating  (xz/xml1-> book-loc :average_rating xz/text)
    :title   (xz/xml1-> book-loc :title xz/text)
    :link (xz/xml1-> book-loc :link xz/text)
    :authors (vec (xz/xml-> book-loc :authors :author :name
                            (fn [n] {:name (xz/xml1-> n :name xz/text)})))}})