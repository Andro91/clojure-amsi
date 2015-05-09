(ns amsi.db
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]))


(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "clojure.db"})


(defn list-users
  "Returns a list of all the users from the database (limited to 1000 for the sake of faster processing)"
  []
  (sql/query db ["select * from triplets LIMIT 1000"]))


(defn select-specific-user
  "Returns records related to the input user"
  [iduser]
  (sql/query db [(str "select * from triplets WHERE iduser = '" iduser "'")]))


(defn ^double sub-primitives
  "Find absolute distance between two numbers as primitives"
  [^double num1 ^double num2]
  (Math/abs (- num1 num2)))


(defn check-similarity
  "Returns a similarity quoeficient between two users"
  [user1 user2]
  (let [resultset1 (sql/query db ["SELECT * FROM triplets WHERE iduser like ?" user1])
        resultset2 (sql/query db [(str "SELECT * FROM triplets WHERE idsong
                        IN (SELECT idsong FROM triplets WHERE iduser like '" user1 "')
                        AND iduser = '" user2 "'")])
        let-list (for [item2 resultset1 :let [y (for [item1 resultset2 :let [z (sub-primitives (:norm item2) (:norm item1))]
                                                                       :when (= (item2 :idsong) (item1 :idsong))]
                                                  z)]]
                   y)]
  (if (pos? (count let-list))
      (/ 1 (inc (/ (reduce + (flatten let-list)) (count let-list))))
      0)))


(defn recommended-songs
  "Creates a list of songs recommended for the user"
  [user-list]
  (let [let-list (for [item user-list :let [results (sql/query db [(str "SELECT idsong FROM triplets WHERE iduser = '" (:iduser item) "'
                                                                    AND number = (SELECT max(number) FROM triplets WHERE iduser = '" (:iduser item) "') LIMIT 1")])
                                            y (assoc (first results) :similarity (:similarity item))]]
                  y)]
  (reverse (sort-by :score (distinct (for [i let-list :let [z (assoc i :score (* (:similarity i) ((frequencies let-list) i)))]]
                                       z))))))


(defn list-similar-users
  "Returns users similar to the input user, based on the number of occurences of a song being heard by both users"
  [iduser]
  (let [results
        (sql/query db
                   [(str "SELECT DISTINCT iduser, count(idsong) as expr
                         FROM triplets
                         WHERE idsong IN (SELECT idsong FROM triplets WHERE iduser like '" iduser "')
                         AND iduser NOT LIKE '" iduser "' GROUP BY iduser ORDER BY expr DESC LIMIT 10")])]
    (for [x results :let [y (assoc x :similarity (check-similarity iduser (:iduser x)))]]
      y)))
