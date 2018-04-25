(ns goodreads.config.connection
  (:require [environ.core :refer [env]]
            [clojure.data.xml :refer :all]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as xz]
            [clj-http.client :as http]
            [manifold.deferred :as d]
            [clojure.string :as str]))

(defn p [res]
  (-> res :body
      java.io.StringReader.
      parse
      z/xml-zip))

(def dkey "paZ3A3dqrc9JDwyfSsTDQ")

(def list-books-by-shelf
  (memoize
    (fn [shelf-name]
      (xz/xml->
        (-> (http/get
              "https://www.goodreads.com/review/list/80237278.xml"
              {:query-params {:v 2 :key dkey
                              :shelf shelf-name}})
            p)
        :reviews
        :review
        :book
        :id
        xz/text))))

(defn map-book-id-to-book [book-loc]
  {(xz/xml1-> book-loc :id xz/text)
   {:rating  (xz/xml1-> book-loc :average_rating xz/text)
    :title   (xz/xml1-> book-loc :title xz/text)
    :link (xz/xml1-> book-loc :link xz/text)
    :authors (vec (xz/xml-> book-loc :authors :author :name
                            (fn [n] {:name (xz/xml1-> n :name xz/text)})))}})

(def find-similar-books
  (memoize
    (fn [book-id]
      (reduce merge
              (xz/xml->
                (-> (http/get
                      (str "https://www.goodreads.com/book/show/" book-id ".xml")
                      {:query-params {:key dkey}})
                    p)
                :book
                :similar_books
                :book
                map-book-id-to-book)))))

(defn build-recommendations []
  (let [read-books (list-books-by-shelf "read")]
    (->> read-books
         (map find-similar-books)
         (reduce merge)
         (remove (fn [[id _]]
                   (some #{id} (list-books-by-shelf "currently-reading"))))
         (sort-by (complement #(:rating (second %))))
         (take 10)
         (map (comp #(dissoc % :rating) second)))))