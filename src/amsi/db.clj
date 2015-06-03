(ns amsi.db
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]
            [korma.core :as kormacore]
            [korma.db :as kormadb]))


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

;;check-similarity-recordset1
(defn user1-data
  "check-similarity data query for user1"
  [user1]
  (sql/query db ["SELECT * FROM triplets WHERE iduser like ?" user1]))

;;check-similarity-recordset2
(defn user2-data
  "check-similarity data query for user2"
  [user1 user2]
  (sql/query db [(str "SELECT * FROM triplets WHERE idsong
                        IN (SELECT idsong FROM triplets WHERE iduser like '" user1 "')
                        AND iduser = '" user2 "'")]))


(defn check-similarity
  "Returns a similarity quoeficient between two users"
  [resultset1 resultset2]
  (let [let-list (for [item2 resultset1 :let [y (for [item1 resultset2 :let [z (sub-primitives (:norm item2) (:norm item1))]
                                                                       :when (= (item2 :idsong) (item1 :idsong))]
                                                  z)]]
                   y)]
  (if (pos? (count let-list))
      (/ 1 (inc (/ (reduce + (flatten let-list)) (count let-list))))
      0)))


(defn recommended-songs
  "Creates a list of songs recommended for the user"
  [user-list]
  (let [let-list (for [item user-list :let [results (sql/query db [(str "SELECT iduser, idsong FROM triplets WHERE iduser = '" (:iduser item) "'
                                                                    AND number = (SELECT max(number) FROM triplets WHERE iduser = '" (:iduser item) "') LIMIT 1")])
                                            y (assoc (first results) :similarity (:similarity item))]]
                  y)]
  (reverse (sort-by :score (distinct (for [i let-list :let [z (assoc i :score (* (:similarity i) ((frequencies let-list) i)))]]
                                       z))))))


;;Function to be passed as the second input parameter to the recommended-songs2
;;Known for query result issues
(defn recommended-recordset
  "Recordset for the recommended-songs2 function"
  [user-list]
  (sql/query db [(str "SELECT iduser, idsong FROM triplets WHERE iduser IN ('"
                      (clojure.string/join "', '" (map :iduser user-list))
                      "') GROUP BY iduser ORDER BY MAX(number) DESC")]))

(defn recommended-recordset2
  "Recordset for the recommended-songs2 function"
  [user-list]
  (declare triplets)
  (kormacore/defentity triplets)
  (doall (kormacore/select triplets (kormacore/fields :iduser :idsong :number)
                  (kormacore/where {:iduser [in (map :iduser user-list)]})
                  (kormacore/order (max :number) :ASC)
                  (kormacore/group :iduser)
                  )))


(defn recommended-songs2
  "Creates a list of songs recommended for the user"
  [user-list results]
  (let [let-list (for [item user-list :let [x (filter #(= (:iduser %) (:iduser item)) results)
                                            y (assoc (first x) :similarity (:similarity item))]]
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
    (for [x results :let [y (assoc x :similarity (check-similarity-datom (user1-data-datom iduser) (user2-data-datom iduser (:iduser x))))]]
       y)))

(defn initialize-datomic
  []
  (def uri "datomic:mem://localhost:4334/clojure")
  (d/delete-database uri)
  (d/create-database uri)
  (def conn (d/connect uri))
  (def schema-tx (read-string (slurp "resources/schema.edn")))
  @(d/transact conn schema-tx)
  (def db (d/db conn))
)

(defn user2-helper
  [user1]
(d/q '[:find [?song ...]
       :in $ ?name
       :where [?e :user/id ?name]
              [?e :user/song ?song]]
     (d/db (d/connect uri)) user1))

(defn user1-data-datom
  "check-similarity data query for user1"
  [user1]
  (d/q '[:find ?song ?norm
       :in $ ?name
       :where [?e :user/id ?name]
              [?e :user/song ?song]
              [?e :user/norm ?norm]]
     (d/db (d/connect uri)) user1))


(defn user2-data-datom
  "check-similarity data query for user2"
  [user1 user2]
  (d/q '[:find ?name ?song ?number ?norm
       :in $ [?song ...] ?name
       :where [?e :user/song ?song]
              [?e :user/id ?name]
              [?e :user/number ?number]
              [?e :user/norm ?norm]
              ]
     (d/db (d/connect uri)) (user2-helper user1) user2))


(defn check-similarity-datom
  "Returns a similarity quoeficient between two users"
  [resultset1 resultset2]
  (let [let-list (for [item2 resultset1 :let [y (for [item1 resultset2 :let [z (db/sub-primitives (nth item2 1) (nth item1 3))]
                                                                       :when (= (first item2) (nth item1 1))]
                                                  z)]]
                   y)]
  (if (pos? (count let-list))
      (/ 1 (inc (/ (reduce + (flatten let-list)) (count let-list))))
      0)))
