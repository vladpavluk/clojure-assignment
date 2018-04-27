(ns goodreads.config.auth
  (:require [environ.core :refer [env]]
            [clojure.data.xml :refer :all]))

(def ^:dynamic *dkey* nil)

(defmacro with-developer-key [token & body]
  `(binding [*dkey* ~token]
     ~@body))