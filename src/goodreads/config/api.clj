(ns goodreads.config.api
  (:use [goodreads.config.interceptors]
        [goodreads.config.auth])
  (:require [environ.core :refer [env]]
            [clojure.data.xml :refer :all]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as xz]
            [clj-http.client :as http]))

(def goodreads-base-url "https://www.goodreads.com/")

(defn goodreads:get
  "Provided a relative URL and query params,
  executes a http-get request with passing the developer key to
  the query params.
  Returns a zipper created from the response body"
  ([url query-params]
   (prn (str goodreads-base-url url) (merge query-params {:key *dkey*}))
   (-> (http/get
         (str goodreads-base-url url)
         {:query-params (merge query-params {:key *dkey*})})
       zip-body))
  ([url]
   (goodreads:get url {})))

(def api:list-books-by-shelf
  "Being passed a user-id and a shelf name, returns a list of
  books for provided user and shelf"
  (memoize
    (fn [user-id shelf-name]
      (xz/xml->
        (goodreads:get
          (str "review/list/" user-id ".xml")
          {:v 2 :shelf shelf-name})
        :reviews
        :review
        :book
        :id
        xz/text))))

(def api:find-similar-books
  "Given a book id, finds a list of all similar books"
  (memoize
    (fn [book-id]
      (reduce
        merge
        (xz/xml->
          (goodreads:get (str "book/show/" book-id ".xml"))
          :book
          :similar_books
          :book
          map-book-id-to-book)))))