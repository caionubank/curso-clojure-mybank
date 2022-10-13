(ns mybank-web-api.core
  (:require [clojure.pprint :as pp]
            [exceptions.core :as ex]
            [db.data :as db])
  (:gen-class))


(defn get-saldo [request]
  (let [id-conta (-> request :path-params :id keyword)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (id-conta @db/contas "conta inválida!")}))

(defn account-exist?
  [id-conta]
  (id-conta @db/contas))

(defn success-response
  [id-conta]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body {:id-conta   id-conta
          :novo-saldo (id-conta @db/contas)}})

(defn make-deposit! [request]
  (let [account-id (-> request :path-params :id keyword)
        deposit-amount (-> request :body slurp parse-double)
        account? (account-exist? account-id)
        active? (:ativo (account-id @db/contas))]
    (cond
      (not account?) ex/account-not-found
      (not active?) ex/blocked-account
      (or (neg? deposit-amount) (= deposit-amount 0.0)) ex/invalid-amount
      :else (try
              (swap! db/contas (fn [m] (update-in m [account-id :saldo] #(+ % deposit-amount))))
              (success-response account-id)
              (catch Exception e
                (ex-info "Error on swap!"
                         {:message (.getMessage e)
                          :class   (.getClass e)}))))))

(defn make-withdraw!
  "Realize a withdraw if an account exist and the account has enough funds"
  [request]
  (let [account-id (-> request :path-params :id keyword)
        withdraw-value (-> request :body slurp parse-double)
        current-ammount (:saldo (account-id @db/contas))
        account? (account-exist? account-id)
        active? (:ativo (account-id @db/contas))]
    (cond
      (not account?) ex/account-not-found
      (not active?) ex/blocked-account
      (< (- current-ammount withdraw-value) 0) ex/insufficient-funds
      (or (neg? withdraw-value) (= withdraw-value 0.0)) ex/invalid-amount
      :else (try
              (swap! db/contas (fn [m] (update-in m [account-id :saldo] #(- % withdraw-value))))
              (success-response account-id)
              (catch Exception e
                (ex-info "Error on swap!"
                         {:message (.getMessage e)
                          :class   (.getClass e)}))))))


(comment
  (catch Throwable e
    (throwable->map e)))