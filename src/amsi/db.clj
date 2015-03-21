(ns amsi.db
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]))


(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/clojurebase"
         :user "root"
         :password ""})


(def UserList (atom []))

(defn list-users
  "Returns a list of all the users from the database"
  []
  (let [results (sql/query db
      ["select * from triplets LIMIT 1000"])]
    results))

(defn check-similarity
  "Returns a similarity quoeficient between two users (UNDER CONSTRUCTION)"
  [user1 user2]
  (def rez
  (sql/query db ["SELECT * FROM triplets WHERE iduser like ?" user1]))
  (def rez2
  (sql/query db [(str "SELECT * FROM triplets WHERE idsong
                  IN (SELECT idsong FROM triplets WHERE iduser like '" user1 "')
                  AND iduser = '" user2 "'")]))
  ;;(println rez)
  ;;(println rez2)
  (def tempList (atom []))
  (doseq [item2 rez2]
    (doseq [item1 rez]
       (do
         (if (= (item2 :idsong) (item1 :idsong))
           (swap! tempList conj (Math/abs (- (double (item2 :norm)) (double (item1 :norm)))) )
           ;;(def var nil)
           )
         ))
    )
  (if (> (count @tempList) 0)
    (do
      (def similarity (/ (reduce + @tempList) (count @tempList)))
      ;;(prn (str "similarnosr je " similarity))
      (/ 1 (+ 1 similarity))
      )
    0
  )
  )

(defn users-by-similarity
  "insert description"
  [user]
  ;;(prn UserList)
  )


(defn list-similar-users
  "Returns users similar to the input user, based on the number of occurences of a song being heard by both users"
  [iduser]
  (let [results-similar
    (sql/query db
      [(str "SELECT DISTINCT iduser, count(idsong) as expr
            FROM triplets
            WHERE idsong IN (SELECT idsong FROM triplets WHERE iduser like '" iduser "')
            AND iduser NOT LIKE '" iduser "' GROUP BY iduser ORDER BY expr DESC LIMIT 10")]
      )]
    ;;(swap! UserList conj results-similar)
    (doseq [item results-similar] (swap! UserList conj (assoc item :similarity (check-similarity iduser (:iduser item)))))
    ;;(swap! UserList conj results-similar)
    (prn UserList)
    (hic-p/html5
     [:table {:class "table"}
      [:tr [:th "user"] [:th "number of same songs"] [:th "similarity quoeficient"]]
      (for [item results-similar]
       [:tr [:td (:iduser item)] [:td (:expr item)] [:td (check-similarity iduser (:iduser item))]])]
     )))


(defn list-user-songs
  "Returns an HTML table, populated with songs listened to by the input user"
  [iduser]
  (let [results
    (sql/query db
      [(str "select * from triplets WHERE iduser = '" (iduser :iduser) "'")]
      )]
    (hic-p/html5
     [:h2 "Songs listened to by the user"]
     [:table {:class "table"}
      [:tr [:th "user"] [:th "song"] [:th "number of time listened"] [:th "normalization"]]
      (for [loc results]
       [:tr [:td (:iduser loc)] [:td (:idsong loc)] [:td (:number loc)] [:td (:norm loc)]])]
     [:br]
     [:h2 "Similar users by songs"]
     (list-similar-users (iduser :iduser))
     )))

;;test poziv funkcija
;;(check-similarity "d7083f5e1d50c264277d624340edaaf3dc16095b" "a5b450f2fcfd35184b1ebde5be09ef931e630522")
;;(list-similar-users "d7083f5e1d50c264277d624340edaaf3dc16095b")
;;(users-by-similarity "d7083f5e1d50c264277d624340edaaf3dc16095b")
;;(list-user-songs {:iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"})
