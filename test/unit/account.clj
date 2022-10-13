(ns unit.account
  (:require [clojure.test :refer :all]
            [mybank-web-api.core :refer :all]
            [server.core :refer :all]
            [db.data :as db]
            [io.pedestal.http :as http]))


(deftest start-server
  (testing "Server has been started"
    (is (= {:status 200,
            :body   "{:saldo 100, :ativo true}"}
           (do (start) (dissoc (test-request server :get "/saldo/1") :headers))))))


(deftest debit
  (testing "debit on account"
    (is (= {:status 200,
            :body "{:id-conta :1, :novo-saldo {:saldo 90.0, :ativo true}}"}
           (dissoc (test-post server :post "/withdraw/1" "10") :headers))))

  (testing "Shouldn't debit non-positive amounts"
    (is (= {:status 400
            :body "Invalid amount"}
           (dissoc (test-post server :post "/withdraw/1" "-10") :headers))))

  (testing "Shouldn't debit zero amount"
    (is (= {:status 400
            :body "Invalid amount"}
           (dissoc (test-post server :post "/withdraw/1" "0") :headers))))

  #_(testing "Sending a non-number on withdraw"
    (is (= {:status 500
            :body "Internal server error"}
           (test-post server :post "/withdraw/1" "f"))))

  (testing "Invalid account for debit"
    (is (= {:status 404
            :body "Account not found"}
           (dissoc (test-post server :post "/withdraw/4" "0") :headers))))

  (testing "User cannot withdraw the amount"
    (is (= {:status 400
            :body "Insufficient funds"}
           (dissoc (test-post server :post "/withdraw/1" "600.0") :headers))))

  (testing "Account is blocked, cannot debit any amount"
    (is (= {:status 400
            :body "Blocked account"}
           (dissoc (test-post server :post "/withdraw/2" "50") :headers)))))

(deftest deposit
  (testing "Verify deposit was made"
    (is (= {:status 200,
            :body   "{:id-conta :1, :novo-saldo {:saldo 100.0, :ativo true}}"}
           (dissoc (test-post server :post "/deposito/1" "10") :headers))))

  (testing "Shouldn't deposit non-positive amounts"
    (is (= {:status 400
            :body "Invalid amount"}
           (dissoc (test-post server :post "/deposito/1" "-10") :headers))))

  (testing "Shouldn't deposit zero amount"
    (is (= {:status 400
            :body "Invalid amount"}
           (dissoc (test-post server :post "/deposito/1" "0") :headers))))

  (testing "Invalid account for deposit"
    (is (= {:status 404
            :body "Account not found"}
           (dissoc (test-post server :post "/deposito/4" "0") :headers))))

  (testing "Account is blocked, cannot deposit any amount"
    (is (= {:status 400
            :body "Blocked account"}
           (dissoc (test-post server :post "/deposito/2" "50") :headers)))))