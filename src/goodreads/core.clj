(ns goodreads.core
  (:use [goodreads.config.connection])
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [manifold.deferred :as d]))

(def cli-options [["-u"
                   "--user-id"
                   "Target User ID"
                   :default 5000]
                  ["-t"
                   "--timeout-ms"
                   "Wait before finished"
                   :default 5000
                   :parse-fn #(Integer/parseInt %)]
                  ["-n"
                   "--number-books"
                   "How many books do you want to recommend"
                   :default 10
                   :parse-fn #(Integer/parseInt %)]
                  ["-h" "--help"]])

(defn book->str [{:keys [title link authors]}]
  (format "\"%s\" by %s\nMore: %s"
          title
          (->> authors
               (map :name)
               (clojure.string/join ", "))
          link))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (contains? options :help) (do (println summary) (System/exit 0))
      (some? errors) (do (println errors) (System/exit 1))
      (empty? args) (do (println "Please, specify user's token") (System/exit 1))      
      :else (let [token {:token (first args)}
                  user-id (:user-id options)
                  number-books (:number-books options)
                  books (-> (build-recommendations token user-id number-books)
                            (d/timeout! (:timeout-ms options) ::timeout)
                            deref)]
              (cond
                (= ::timeout books) (println "Not enough time :(")                
                (empty? books) (println "Nothing found, leave me alone :(")
                :else (doseq [[i book] (map-indexed vector books)]
                        (println (str "#" (inc i)))
                        (println (book->str book))
                        (println)))))))

;(build-recommendations "paZ3A3dqrc9JDwyfSsTDQ" "80237244")