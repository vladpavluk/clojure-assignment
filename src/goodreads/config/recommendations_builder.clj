(ns goodreads.config.recommendations_builder
  (:use [goodreads.config.api]
        [goodreads.config.auth])
  (:require [environ.core :refer [env]]
            [manifold.deferred :as d]
            [clojure.data.xml :refer :all]))

(def ^:private +read-shelf+ "read")
(def ^:private +currently-reading-shelf+ "currently-reading")

(defn build-recommendations [{:keys [token user-id number-books] :as options}]
  (->>
    (let [read-books (api:list-books-by-shelf user-id +read-shelf+)]
      (->> read-books
           (map api:find-similar-books)
           (reduce merge)
           (remove (fn [[id _]]
                     (some #{id} (api:list-books-by-shelf user-id +currently-reading-shelf+))))
           (sort-by (complement #(:rating (second %))))
           (take number-books)
           (map (comp #(dissoc % :rating) second))))
    (with-developer-key token)
    d/success-deferred))