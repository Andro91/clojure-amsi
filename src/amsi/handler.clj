(ns amsi.handler
  (:require [compojure.core :refer :all]
            [amsi.views :as views]
            [amsi.db :as db]
            [compojure.route :as route]
            [compojure.handler :as handler]
            ))

(defroutes app-routes
  (GET "/"
       []
       (views/home-page))
  (GET "/all-users"
       []
       (views/all-users-page))
  (GET "/select-user"
       []
       (views/select-user-page))
  (POST "/ajaxcall"
        {params :params}
        (db/list-user-songs params))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
