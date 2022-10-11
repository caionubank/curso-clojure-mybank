(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [mybank-web-api.core :as mb]))


(def routes
  (route/expand-routes
    #{["/saldo/:id" :get mb/get-saldo :route-name :saldo]
      ["/deposito/:id" :post mb/make-deposit! :route-name :deposito]
      ["/withdraw/:id" :post mb/make-withdraw! :route-name :withdraw]}))
