(ns amsi.db
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/clojurebase"
         :user "root"
         :password ""})


(defn add-user-to-db
  [n l]
  (let [results (sql/with-connection db
                  (sql/insert-record :users
                                     {:name n :last_name l}))]
    (assert (= (count results) 1))
    (first (vals results))))


(defn list-users []
  (let [results (sql/with-connection db
    (sql/with-query-results rows
      ["select * from triplets LIMIT 1000"]
      (doall rows)))]
    results))


(defn list-user-songs [iduser]
  (let [results (sql/with-connection db
    (sql/with-query-results rows
      [(str "select * from triplets WHERE iduser = '" (iduser :iduser) "'")]
      (doall rows)))]
    (hic-p/html5
     [:table {:class "table"}
      [:tr [:th "user"] [:th "song"] [:th "number of time listened"] [:th "normalization"]]
      (for [loc results]
       [:tr [:td (:iduser loc)] [:td (:idsong loc)] [:td (:number loc)] [:td (:norm loc)]])]
     )))
