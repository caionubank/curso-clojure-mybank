(ns db.data)

(defonce contas (atom {:1 {:saldo 100 :ativo true}
                       :2 {:saldo 200 :ativo false}
                       :3 {:saldo 300 :ativo true}}))