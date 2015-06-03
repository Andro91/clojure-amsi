(ns amsi.test
  (:require [clojure.java.jdbc :as sql]
            [hiccup.page :as hic-p]
            [criterium.core :as crit-core]
            [midje.sweet :as sweet]
            [amsi.db :as db]
            [korma.core :as kormacore]
            [korma.db :as kormadb]
            [datomic.api :only (db q) :as d]
            [clojure.java.jdbc :as sql]))

(def dbs
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "clojure.db"})

;;Execution time mean : 6.106240 ns
;;   Execution time std-deviation : 0.011833 ns
;;   Execution time lower quantile : 6.090829 ns ( 2.5%)
;;   Execution time upper quantile : 6.132613 ns (97.5%)
;;                   Overhead used : 1.885398 ns
;;(crit-core/bench db/list-users)

;;Execution time mean : 1.382855 sec
;;   Execution time std-deviation : 17.946696 ms
;;   Execution time lower quantile : 1.346695 sec ( 2.5%)
;;   Execution time upper quantile : 1.405737 sec (97.5%)
;;                   Overhead used : 1.885398 ns
;;(crit-core/bench (db/check-similarity "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d" "d68dc6fc25248234590d7668a11e3335534ae4b4"))


;; Execution time mean : 4.275240 min
;;   Execution time std-deviation : 30.798197 min
;;   Execution time lower quantile : 17.445581 sec ( 2.5%)
;;   Execution time upper quantile : 20.338244 sec (97.5%)
;;                   Overhead used : 1.885398 ns
;;(crit-core/bench (db/recommended-songs (db/list-similar-users "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d")))


;;Execution time mean : 1.796394 ms
;;    Execution time std-deviation : 66.240417 µs
;;   Execution time lower quantile : 1.768789 ms ( 2.5%)
;;   Execution time upper quantile : 1.840001 ms (97.5%)
;;                   Overhead used : 1.912597 ns
;;(crit-core/bench (db/recommended-songs2 (db/list-similar-users "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d") (db/recommended-recordset (db/list-similar-users "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"))))


;;def iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d")

;;(defn somert [sum cnt]
;;    ; If count reaches 0 then exit the loop and return sum
;;    (if (= (count cnt) 0)
;;    sum
;;    ; Otherwise add count to sum, decrease count and
;;    ; use recur to feed the new values back into the loop
;;    (recur (conj sum (assoc (first cnt) :similarity (db/check-similarity (db/user1-data iduser) (db/user2-data iduser (:iduser (first cnt)))))) (rest cnt))))



;;(time (filter #(= (:iduser %) (:iduser {:iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"})) database))



;;(def database 3)
;;(def database (sql/query dbs ["SELECT * FROM triplets"]))
;;(def onehunid (sql/query dbs ["SELECT * FROM triplets limit 100"]))


;;(defn user1-data
;;  "check-similarity data query for user1"
;;  [user1]
;;  (filter #(= (:iduser %) user1) database))

;;(defn user1-data
;;  "check-similarity data query for user1"
;;  [user1]
;;  (d/q '[:find ?song
;;       :in $ ?name
;;       :where [?e :user/id ?name]
;;              [?e :user/song ?song]]
;;     (d/db (d/connect uri)) user1))

;;(map :idsong (user1-data "8808c596872da94c7efdc32afd51c73800da0b55"))


;;(some #(= "SOEGIYH12A6D4FC0E3" %) (map :idsong (user1-data "14d4743dd152529292dbbb4eac9273bdcf55630c")))

;;(defn u2d-helper
;;  [f u]
;;  (some #(= (:idsong f) %) (map :idsong (user1-data u)))
;;  )

;;(defn user2-data
;;  "check-similarity data query for user2"
;;  [user1 user2]
;;  (filter #(u2d-helper % user1) (filter #(= (:iduser %) user2) database)))

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

;;(check-similarity-datom
;; (user1-data-datom "b4cacb67298373df24d03e0fe7c152e0ab3ff340")
;; (user2-data-datom "b4cacb67298373df24d03e0fe7c152e0ab3ff340" "195fdee0cdcb496fbd18a26f3290ea64aff54418"))

 ;;(time (db/recommended-songs (list-similar-users "8808c596872da94c7efdc32afd51c73800da0b55")))

;; (time (db/recommended-songs (db/list-similar-users "8808c596872da94c7efdc32afd51c73800da0b55")))

;;(time (d/q '[:find ?name ?song
;;       :in $ [?song ...] ?name
;;       :where [?e :user/song ?song]
;;              [?e :user/id ?name]
;;              ]
;;     (d/db (d/connect uri)) '("SOUCHPA12AB0184B1A" "SOHXWSB12A6D4F7820") "8808c596872da94c7efdc32afd51c73800da0b55"))


;;(sql/query dbs ["select * from triplets LIMIT 1000"])

;;(count (d/q '[:find ?id
;;       :where [?e :user/id ?id]
;;              [?e :user/song ?song]]
;;      (d/db (d/connect uri))))

;;(count database)

;;(def coll (take 1400 (iterate inc 0)))

;;(for
;; [item coll]
;;; (map #(d/transact conn [{:db/id (d/tempid :db.part/user) :user/id (:iduser %) :user/song (:idsong %) :user/number (:number %) :user/norm (:norm %)}])
;; (sql/query dbs [(str "SELECT * FROM triplets limit 1000 offset " (* item 1000))])))


;;A test used to test the testing package. Just for testing purposes. Testing 1 2 3, test, test.
;;Expecting to get true.
(sweet/fact (+ 2 2) => 4)

;;sub-primitives test
(sweet/fact
 (db/sub-primitives 5.0 1.0) => 4.0
 (db/sub-primitives 1.0 5.0) => 4.0
 (db/sub-primitives 3.0 6.0) => 3.0
 (db/sub-primitives 6.0 3.0) => 3.0
 (db/sub-primitives -2.0 3.0) => 5.0
 (db/sub-primitives 3.0 -2.0) => 5.0)

;;test recordsets for the check-similarity fact
(def recordset1
 '({:norm 5.0, :number 1, :idsong "SOBONKR12A58A7A7E0", :iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"}
   {:norm 5.0, :number 1, :idsong "SOEGIYH12A6D4FC0E3", :iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"}
   {:norm 5.0, :number 1, :idsong "SOFLJQZ12A6D4FADA6", :iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"}
   {:norm 5.0, :number 1, :idsong "SOHTKMO12AB01843B0", :iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"}
   {:norm 5.0, :number 1, :idsong "SODQZCY12A6D4F9D11", :iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"}
   {:norm 5.0, :number 1, :idsong "SOXLOQG12AF72A2D55", :iduser "fd50c4007b68a3737fe052d5a4f78ce8aa117f3d"}))

(def recordset2
 '({:norm 5.0, :number 1, :idsong "SOEGIYH12A6D4FC0E3", :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}
   {:norm 5.0, :number 1, :idsong "SOBONKR12A58A7A7E0", :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}
   {:norm 5.0, :number 1, :idsong "SOFLJQZ12A6D4FADA6", :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}
   {:norm 5.0, :number 1, :idsong "SOXLOQG12AF72A2D55", :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}))

(def recordset3
  '({:norm 5.0, :number 1, :idsong "SOBONKR12A58A7A7E0", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
    {:norm 5.0, :number 1, :idsong "SOXLOQG12AF72A2D55", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
    {:norm 5.005422993, :number 2, :idsong "SOFLJQZ12A6D4FADA6", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
    {:norm 5.0, :number 1, :idsong "SOHTKMO12AB01843B0", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
    {:norm 5.157266811, :number 30, :idsong "SOEGIYH12A6D4FC0E3", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}))

;;namespace amsi.db/check-similarity test
(sweet/fact
 (db/check-similarity recordset1 recordset2) => 1.0
 (db/check-similarity recordset1 recordset3) => 0.9736008448949672)


;;Song recordset to test the song recommendation function
(def song-recordset
 '({:idsong "SOBJMRV12A6D4FAB28", :iduser "1659a1533d3cce2f9a33bf678e2fbbd9ef049269"}
   {:idsong "SOEGIYH12A6D4FC0E3", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
   {:idsong "SOQZYQH12A8AE468E5", :iduser "ed23fb14028d9afe701806ebdcd4e2a2cc5b3d16"}
   {:idsong "SOFLJQZ12A6D4FADA6", :iduser "0f8fe483a6d1562f10d794fa894614b6aae54bbd"}
   {:idsong "SOFZSAT12AF72A0806", :iduser "feb8a6a9c06e5b5a5f1ff6f213333c46590e23c0"}
   {:idsong "SONHWUN12AC468C014", :iduser "03e9a39b14d23aef5355ee78f8df91a5b1c3b655"}
   {:idsong "SOALKBV12A6D4F6EE2", :iduser "1527d8f12936592265a8fa9f937fc759b2020606"}
   {:idsong "SOJKRSH12AB0181C39", :iduser "e6173a4e2883fb797950ad00d0492fe691cbc07d"}
   {:idsong "SOXLRDB12A81C21739", :iduser "01f09bf443fa224900c3aba7d0206a53616506a1"}
   {:idsong "SOKSQYH12AB018A1C4", :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}))

;;User recordset to test the song recommendation function
(def user-recordset
  '({:similarity 0.9937129517229777, :expr 5, :iduser "01f09bf443fa224900c3aba7d0206a53616506a1"}
   {:similarity 0.9736008448949672, :expr 5, :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
   {:similarity 0.9946062572292895, :expr 5, :iduser "e6173a4e2883fb797950ad00d0492fe691cbc07d"}
   {:similarity 0.9710373884525068, :expr 5, :iduser "ed23fb14028d9afe701806ebdcd4e2a2cc5b3d16"}
   {:similarity 0.9990969840082338, :expr 5, :iduser "feb8a6a9c06e5b5a5f1ff6f213333c46590e23c0"}
   {:similarity 0.9955011699373647, :expr 4, :iduser "03e9a39b14d23aef5355ee78f8df91a5b1c3b655"}
   {:similarity 0.9866238632532379, :expr 4, :iduser "0f8fe483a6d1562f10d794fa894614b6aae54bbd"}
   {:similarity 1.0,                :expr 4, :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}
   {:similarity 0.9955011701025352, :expr 4, :iduser "1527d8f12936592265a8fa9f937fc759b2020606"}
   {:similarity 0.9928212494212428, :expr 4, :iduser "1659a1533d3cce2f9a33bf678e2fbbd9ef049269"}))

;;Expected recommendation result, based on manualy testing the algorithm
(def expected-recommendation
  '({:score 1.0,                :similarity 1.0,                :idsong "SOKSQYH12AB018A1C4", :iduser "14d4743dd152529292dbbb4eac9273bdcf55630c"}
    {:score 0.9990969840082338, :similarity 0.9990969840082338, :idsong "SOFZSAT12AF72A0806", :iduser "feb8a6a9c06e5b5a5f1ff6f213333c46590e23c0"}
    {:score 0.9955011701025352, :similarity 0.9955011701025352, :idsong "SOALKBV12A6D4F6EE2", :iduser "1527d8f12936592265a8fa9f937fc759b2020606"}
    {:score 0.9955011699373647, :similarity 0.9955011699373647, :idsong "SONHWUN12AC468C014", :iduser "03e9a39b14d23aef5355ee78f8df91a5b1c3b655"}
    {:score 0.9946062572292895, :similarity 0.9946062572292895, :idsong "SOJKRSH12AB0181C39", :iduser "e6173a4e2883fb797950ad00d0492fe691cbc07d"}
    {:score 0.9937129517229777, :similarity 0.9937129517229777, :idsong "SOXLRDB12A81C21739", :iduser "01f09bf443fa224900c3aba7d0206a53616506a1"}
    {:score 0.9928212494212428, :similarity 0.9928212494212428, :idsong "SOBJMRV12A6D4FAB28", :iduser "1659a1533d3cce2f9a33bf678e2fbbd9ef049269"}
    {:score 0.9866238632532379, :similarity 0.9866238632532379, :idsong "SOFLJQZ12A6D4FADA6", :iduser "0f8fe483a6d1562f10d794fa894614b6aae54bbd"}
    {:score 0.9736008448949672, :similarity 0.9736008448949672, :idsong "SOEGIYH12A6D4FC0E3", :iduser "b9afd265a5873dd193ee3f348f539b54f42835ea"}
    {:score 0.9710373884525068, :similarity 0.9710373884525068, :idsong "SOQZYQH12A8AE468E5", :iduser "ed23fb14028d9afe701806ebdcd4e2a2cc5b3d16"}))


;;Testing the two variants of the song recommendation algorithm.
;;Under controled conditions, they return the same results.
;;The second function is better database-wise, but displays strange behavior regarding query results.
(sweet/fact
 (db/recommended-songs  user-recordset)                => expected-recommendation
 (db/recommended-songs2 user-recordset song-recordset) => expected-recommendation)
