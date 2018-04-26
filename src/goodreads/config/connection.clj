(ns goodreads.config.connection
  (:use [goodreads.config.api]
        [goodreads.config.auth])
  (:require [environ.core :refer [env]]
            [clojure.data.xml :refer :all]))

(defn build-recommendations [developer-key user-id number-books]
  (with-developer-key
    developer-key
    (let [read-books (api:list-books-by-shelf user-id "read")]
      (->> read-books
           (map api:find-similar-books)
           (reduce merge)
           (remove (fn [[id _]]
                     (some #{id} (api:list-books-by-shelf user-id "currently-reading"))))
           (sort-by (complement #(:rating (second %))))
           (take number-books)
           (map (comp #(dissoc % :rating) second))))))