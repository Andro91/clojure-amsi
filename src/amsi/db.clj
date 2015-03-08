(ns amsi.db
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]))


(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/clojurebase"
         :user "root"
         :password ""})


(defn list-users
  "Funkcija koja izlistava sve korisnike u bazi"
  []
  (let [results (sql/query db
      ["select * from triplets LIMIT 1000"])]
    results))

(defn list-similar-users
  "Funkcija koja vraca HTML5 tabelu koja sadrzi korisnike koji su slusali iste pesme kao dati korisnik"
  [iduser]
  (let [results-similar
    (sql/query db
      [(str "SELECT DISTINCT iduser, count(idsong) as expr
            FROM triplets
            WHERE idsong IN (SELECT idsong FROM triplets WHERE iduser like '" (iduser :iduser) "')
            AND iduser NOT LIKE '" (iduser :iduser) "' GROUP BY iduser ORDER BY expr DESC LIMIT 10")]
      )]
    (hic-p/html5
     [:table {:class "table"}
      [:tr [:th "user"] [:th "number of same songs"]]
      (for [loc results-similar]
       [:tr [:td (:iduser loc)] [:td (:expr loc)]])]
     )))


(defn list-user-songs
  "Spisak svih pesama koje je dati korisnik odslusao, u formatu HTML5 tabele"
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
     (list-similar-users iduser)
     )))


(defn check-similarity
  "Funkcija za proveru slicnosti izmedju dva korisnika (UNDER CONSTRUCTION)"
  [user1 user2]
  (def rez
  (sql/query db ["SELECT * FROM triplets WHERE iduser like ?" user1]))
  (def rez2
  (sql/query db [(str "SELECT * FROM triplets WHERE idsong
                  IN (SELECT idsong FROM triplets WHERE iduser like '" user1 "')
                  AND iduser = '" user2 "'")]))
  (println rez)
  (println rez2)
  (def praznaLista (atom []))
  (doseq [ item2 rez2 ] (doseq [item1 rez] (do (if (= (item2 :idsong) (item1 :idsong)) (swap! praznaLista conj (- (bigdec (item2 :number)) (bigdec (item1 :number))) ) (def var nil)) (prn praznaLista) ))
  )
  (prn praznaLista)
  (def similarity (/ (reduce + @praznaLista) (count @praznaLista)))
  similarity
  )


;;test poziv funkcija
(check-similarity "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d" "ed23fb14028d9afe701806ebdcd4e2a2cc5b3d16")

(list-user-songs {:iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"})
