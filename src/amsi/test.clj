(ns amsi.test
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]
            [criterium.core :as crit-core]
            [amsi.db :as db]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "clojure.db"
   })



;;Execution time mean : 6.106240 ns
;;   Execution time std-deviation : 0.011833 ns
;;   Execution time lower quantile : 6.090829 ns ( 2.5%)
;;   Execution time upper quantile : 6.132613 ns (97.5%)
;;                   Overhead used : 1.885398 ns
(crit-core/bench db/list-users)

;;Execution time mean : 1.382855 sec
;;   Execution time std-deviation : 17.946696 ms
;;   Execution time lower quantile : 1.346695 sec ( 2.5%)
;;   Execution time upper quantile : 1.405737 sec (97.5%)
;;                   Overhead used : 1.885398 ns
(crit-core/bench (db/check-similarity "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d" "d68dc6fc25248234590d7668a11e3335534ae4b4"))


;; Execution time mean : 4.275240 min
;;   Execution time std-deviation : 30.798197 min
;;   Execution time lower quantile : 17.445581 sec ( 2.5%)
;;   Execution time upper quantile : 20.338244 sec (97.5%)
;;                   Overhead used : 1.885398 ns
(crit-core/bench (db/recommended-songs2 (db/list-similar-users "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d")))
