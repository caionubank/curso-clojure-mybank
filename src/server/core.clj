(ns server.core
  (:require [io.pedestal.http :as http]
            [io.pedestal.test :as test-http]
            [io.pedestal.interceptor :as i]
            [server.routes :as rt]
            [interceptors.core :as intercp]))


(defonce server (atom nil))

(def service-map-simple {::http/routes rt/routes
                         ::http/port   9999
                         ::http/type   :jetty
                         ::http/join?  false})

(def service-map (-> service-map-simple
                     (http/default-interceptors)
                     (update ::http/interceptors conj (i/interceptor intercp/contas-interceptor))))

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
  (http/stop @srv/server)
  (restart-server)

  ; Getting User
  (test-request server :get "/saldo/1")
  (test-request server :get "/saldo/2")
  (test-request server :get "/saldo/3")
  (test-request server :get "/saldo/4")

  ; Deposit
  (test-post server :post "/deposito/1" "199.93")
  (test-post server :post "/deposito/4" "325.99")

  ; Withdraw
  (test-post server :post "/withdraw/1" "12.93")
  (test-post server :post "/withdraw/2" "10.93")
  (test-post server :post "/withdraw/4" "299.93")

  ;curl http://localhost:9999/saldo/1
  ;curl -d "199.99" -X POST http://localhost:9999/deposito/1
  )