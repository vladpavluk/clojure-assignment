(ns goodreads.config.connection
  (:require [clojure.tools.cli :as cli]
            [environ.core :refer [env]]
            [oauth.client :as oauth]
            [clojure.data.xml :refer :all]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as xz]
            [clj-http.client :as http]
            [manifold.deferred :as d]))

(defn p [res]
  (-> res :body java.io.StringReader.
      parse
      z/xml-zip))

#_(def ^:private gr-creds
  {:key    (env :gr-key)
   :secret (env :gr-secret)})

(defonce consumer
  (oauth/make-consumer
    (:key gr-creds)
    (:secret gr-creds)
    "https://www.goodreads.com/oauth/request_token"
    "https://www.goodreads.com/oauth/access_token"
    "https://www.goodreads.com/oauth/authorize"
    :hmac-sha1))

(def request-token (oauth/request-token consumer))

(prn (oauth/user-approval-uri consumer (:oauth_token request-token)))

(def access-token-response
  (oauth/access-token consumer request-token))

(def res
  (http/get "https://www.goodreads.com/api/auth_user"
            {:headers
             {"Authorization"
              (oauth/authorization-header
                (oauth/credentials
                  consumer
                  (:oauth_token access-token-response)
                  (:oauth_token_secret access-token-response)
                  :GET
                  "https://www.goodreads.com/api/auth_user"
                  {}))}}))

(def uid
  (xml1-> (p res)
          :user
          (attr :id)))

(def shelves
  (http/get "https://www.goodreads.com/shelf/list.xml?key=paZ3A3dqrc9JDwyfSsTDQ"
            {:headers
             {"Authorization"
              (oauth/authorization-header
                (oauth/credentials
                  consumer
                  (:oauth_token access-token-response)
                  (:oauth_token_secret access-token-response)
                  :GET
                  "https://www.goodreads.com/shelf/list.xml?key=paZ3A3dqrc9JDwyfSsTDQ"
                  {:key "paZ3A3dqrc9JDwyfSsTDQ"}))}}))
