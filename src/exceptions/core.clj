(ns exceptions.core)

(def account-not-found
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Account not found"})


(def invalid-account
  {:status 400
   :headers {"Content-Type" "text/plain"}
   :body "Invalid account"})


(def insufficient-funds
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Insufficient funds"})


(def blocked-account
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Blocked account"})