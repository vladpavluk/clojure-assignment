(ns goodreads.config.connection
  (:require [clojure.tools.cli :as cli]
            [environ.core :refer [env]]
            [oauth.client :as oauth]
            [clj-http.client :as http]
            [manifold.deferred :as d]))

(def ^:private gr-creds
  {:key    (env :gr-key)
   :secret (env :gr-secret)})

(def consumer
  (oauth/make-consumer
    (:key gr-creds)
    (:secret gr-creds)
    "https://www.goodreads.com/oauth/request_token"
    "https://www.goodreads.com/oauth/access_token"
    "https://www.goodreads.com/oauth/authorize"
    :hmac-sha1))

(def request-token (oauth/request-token consumer))

(def access-token-response
  (oauth/access-token consumer request-token))

(def credentials
  (oauth/credentials
    consumer
    (:oauth_token access-token-response)
    (:oauth_token_secret access-token-response)
    :GET
    "https://www.goodreads.com/api/auth_user"
    {}))

(http/get "https://www.goodreads.com/api/auth_user"
           {:headers
            {"Authorization" (oauth/authorization-header credentials)}})
