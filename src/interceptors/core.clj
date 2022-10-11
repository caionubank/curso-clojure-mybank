(ns interceptors.core
  (:require [db.data :as db]))


(defn add-contas-atom [context]
  (update context :request assoc :contas db/contas))

(def contas-interceptor
  {:name  :contas-interceptor
   :enter add-contas-atom})