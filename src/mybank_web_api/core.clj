(ns mybank-web-api.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test-http]
            [io.pedestal.interceptor :as i]
            [clojure.pprint :as pp]
            [exceptions.core :as ex])
  (:gen-class))

(defonce server (atom nil))

(defonce contas (atom {:1 {:saldo 100 :ativo true}
                       :2 {:saldo 200 :ativo false}
                       :3 {:saldo 300 :ativo true}}))

(defn add-contas-atom [context]
  (update context :request assoc :contas contas))

(def contas-interceptor
  {:name  :contas-interceptor
   :enter add-contas-atom})

(defn get-saldo [request]
  (let [id-conta (-> request :path-params :id keyword)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (id-conta @contas "conta inválida!")}))

(defn account-exist?
  [id-conta]
  (id-conta @contas))

(defn success-response
  [id-conta]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body {:id-conta   id-conta
          :novo-saldo (id-conta @contas)}})

(defn make-deposit! [request]
  (let [account-id (-> request :path-params :id keyword)
        valor-deposito (-> request :body slurp parse-double)
        account? (account-exist? account-id)
        active? (:ativo (account-id @contas))]
    (cond
      (not account?) ex/account-not-found
      (not active?) ex/blocked-account
      :else (try
              (swap! contas (fn [m] (update-in m [account-id :saldo] #(+ % valor-deposito))))
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
        current-ammount (:saldo (account-id @contas))
        account? (account-exist? account-id)
        active? (:ativo (account-id @contas))]
    (cond
      (not account?) ex/account-not-found
      (not active?) ex/blocked-account
      (< (- current-ammount withdraw-value) 0) ex/insufficient-funds
      :else (try
              (swap! contas (fn [m] (update-in m [account-id :saldo] #(- % withdraw-value))))
              (success-response account-id)
              (catch Exception e
                (ex-info "Error on swap!"
                         {:message (.getMessage e)
                          :class   (.getClass e)}))))))

(def routes
  (route/expand-routes
    #{["/saldo/:id" :get get-saldo :route-name :saldo]
      ["/deposito/:id" :post make-deposit! :route-name :deposito]
      ["/withdraw/:id" :post make-withdraw! :route-name :withdraw]}))


(def service-map-simple {::http/routes routes
                         ::http/port   9999
                         ::http/type   :jetty
                         ::http/join?  false})

(def service-map (-> service-map-simple
                     (http/default-interceptors)
                     (update ::http/interceptors conj (i/interceptor contas-interceptor))))

(defn create-server []
  (http/create-server
    service-map))

(defn start []
  (reset! server (http/start (create-server))))

(defn test-request [server verb url]
  (test-http/response-for (::http/service-fn @server) verb url))
(defn test-post [server verb url body]
  (test-http/response-for (::http/service-fn @server) verb url :body body))

(defn restart-server []
  (try
    (http/stop @server)
    (catch Exception e
      e))
  (try
    (start)
    (catch Exception e
      e)))

(comment
  (start)
  (http/stop @server)
  (restart-server)

  (test-request server :get "/saldo/1")
  (test-request server :get "/saldo/2")
  (test-request server :get "/saldo/3")
  (test-request server :get "/saldo/4")
  (test-post server :post "/deposito/1" "199.93")
  (test-post server :post "/deposito/4" "325.99")
  (test-post server :post "/withdraw/1" "1200.93")
  (test-post server :post "/withdraw/2" "10.93")
  (test-post server :post "/withdraw/4" "299.93")

  ;curl http://localhost:9999/saldo/1
  ;curl -d "199.99" -X POST http://localhost:9999/deposito/1
  )
