(ns goodreads.config.connection
  (:require [clojure.tools.cli :as cli]
            [environ.core :refer [env]]
            [oauth.client :as oauth]
            [clj-http.client :as http]
            [manifold.deferred :as d]))

(def ^:private gr-creds
  {:key (env :gr-key)
   :secret (env :gr-secret)})

(def ^:private gr-creds
  {:key "*******"
   :secret "*******"})

(def consumer (oauth/make-consumer (:key gr-creds)
                                   (:secret gr-creds)
                                   "https://www.goodreads.com/oauth/request_token"
                                   "https://www.goodreads.com/oauth/access_token"
                                   "https://www.goodreads.com/oauth/authorize"
                                   :hmac-sha1))

(def request-token (oauth/request-token consumer))

(def credentials (oauth/credentials consumer
                                    (:oauth_token request-token)
                                    (:oauth_token_secret request-token)
                                    :GET
                                    "https://www.goodreads.com/owned_books/user?format=xml&id=80237278"
                                    {}))

(http/get "https://www.goodreads.com/owned_books/user?format=xml&id=80237278"
           {:query-params credentials})