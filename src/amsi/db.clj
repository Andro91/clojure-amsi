(ns amsi.db
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]))


(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/clojurebase"
         :user "root"
         :password ""})


(def userList (atom []))
(def recommendedSongList (atom []))

(defn list-users
  "Returns a list of all the users from the database"
  []
  (let [results (sql/query db
      ["select * from triplets LIMIT 1000"])]
    results))


(defn check-similarity
  "Returns a similarity quoeficient between two users"
  [user1 user2]
  (def resultset1
  (sql/query db ["SELECT * FROM triplets WHERE iduser like ?" user1]))
  (def resultset2
  (sql/query db [(str "SELECT * FROM triplets WHERE idsong
                  IN (SELECT idsong FROM triplets WHERE iduser like '" user1 "')
                  AND iduser = '" user2 "'")]))
  (def tempList (atom []))
  (doseq [item2 resultset1]
    (doseq [item1 resultset2]
       (do
         (if (= (item2 :idsong) (item1 :idsong))
           (swap! tempList conj (Math/abs (- (double (item2 :norm)) (double (item1 :norm)))) )
           ))))
  (if (> (count @tempList) 0)
    (do
      (def similarity (/ (reduce + @tempList) (count @tempList)))
      (/ 1 (+ 1 similarity)))
    0))


(defn recommended-songs
  "insert description"
  []
  (doseq [item @userList]
    (def recSong
        (sql/query db [(str "SELECT idsong FROM triplets WHERE iduser = '" (item :iduser) "'
                        AND number = (SELECT max(number) FROM triplets WHERE iduser = '" (item :iduser) "') LIMIT 1")]))
    (swap! recommendedSongList conj (assoc (first recSong) :similarity (:similarity item)))
    )
  (def tList (atom []))
  (doseq [i @recommendedSongList]
    (swap! tList conj (assoc i :score (* (:similarity i) ((frequencies @recommendedSongList) i))))
    )
  (reverse (sort-by :score (distinct @tList)))
  )

;;test function call
;;(prn userList)
;;(count @userList)
;;(prn recommendedSongList)
;;(count @recommendedSongList)
;;(count (distinct @recommendedSongList))
;;(recommended-songs)


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
    (doseq [item results-similar] (swap! userList conj (assoc item :similarity (check-similarity iduser (:iduser item)))))
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

;;test function call
;;(check-similarity "d7083f5e1d50c264277d624340edaaf3dc16095b" "a5b450f2fcfd35184b1ebde5be09ef931e630522")
;;(list-similar-users "d7083f5e1d50c264277d624340edaaf3dc16095b")
;;(list-user-songs {:iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"})
