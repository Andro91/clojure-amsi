(ns amsi.views
  (:require [amsi.db :as db]
            [amsi.test :as atest]
            [clojure.string :as str]
            [hiccup.page :as hic-p]))

(defn gen-page-head
  "Generates page headers"
  [title]
  [:head
   [:title title]
   (hic-p/include-css "/bootstrap.min.css")
   (hic-p/include-js "https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js")
   (hic-p/include-js "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js")
   (hic-p/include-css "/style.css")])

(def navbar
  "Generates a navbar"
  [:nav {:class "navbar navbar-default navbar-fixed-top"}
   [:div {:class "container"}
   [:div {:class "navbar-header"}
   [:button {:type "button" :class "navbar-toggle collapsed" :dat-target "#navbar"}
    [:span {:class "sr-only"}]
    [:span {:class "icon-bar"}]
    [:span {:class "icon-bar"}]
    [:span {:class "icon-bar"}]]
    [:a {:class "navbar-brand" :href "#"} [:img {:src "/clojure-icon.gif" :style "height: 20px; float: left; margin-right: 5px"}] "Clojure"]]
    [:div {:id "navbar" :class "collapse navbar-collapse"}
    [:ul {:class "nav navbar-nav"}
     [:li [:a {:href "/"} "Home"]]
     [:li [:a {:href "/select-user"} "Select a User"]]
     [:li [:a {:href "/all-users"} "All Users (TOP 1000)"]]]]]])


(defn home-page
  "Generates the home page content"
  []
  (hic-p/html5
   (gen-page-head "Home")
   navbar
   [:div {:class "container"}
   [:h1 "Home"]
   [:p "Wellcome to Clojure web app. Consult the README for more details."]]))



(defn select-user-page
  "Generates the single user page view"
  []
  (hic-p/html5
   (gen-page-head "Select a USER")
   navbar
   [:div {:class "container"}
   [:h1 "Select a User"]
   [:form {:action "/add-user" :method "POST" :class "form"}
    [:div {:class "form-group"}
    [:label "UserID: " ][:input {:type "text" :id "iduser" :name "name" :class "form-control"}]]
    [:p [:div {:class "btn btn-lg btn-success" :id "submituser"} "Submit User"]]]
    [:div {:id "div1"} [:div {:id "loaderdiv" :class "windows8" :style "display: none;"}
                        [:div {:class "wBall" :id "wBall_1"}
                         [:div {:class "wInnerBall"}]]

                        [:div {:class "wBall" :id "wBall_2"}
                         [:div {:class "wInnerBall"}]]

                        [:div {:class "wBall" :id "wBall_3"}
                         [:div {:class "wInnerBall"}]]

                        [:div {:class "wBall" :id "wBall_4"}
                         [:div {:class "wInnerBall"}]]

                        [:div {:class "wBall" :id "wBall_5"}
                         [:div {:class "wInnerBall"}]]
                        ]]]
    (hic-p/include-js "/ajax.js")))


(defn all-users-page
  "Genearates the content for the all users view page"
  []
  (let [all-locs (db/list-users)]
    (hic-p/html5
     (gen-page-head "Users")
     navbar
     [:div {:class "container"}
     [:h1 "All Users"]
     [:table {:class "table"}
      [:tr [:th "user"] [:th "song"] [:th "number of time listened"] [:th "normalization"]]
      (for [loc all-locs]
        [:tr [:td (:iduser loc)] [:td (:idsong loc)] [:td (:number loc)] [:td (:norm loc)]])]])))

(defn all-users-page-datom
  "Genearates the content for the all users view page"
  []
  (let [all-locs (atest/list-users)]
    (hic-p/html5
     (gen-page-head "Users")
     navbar
     [:div {:class "container"}
     [:h1 "All Users"]
     [:table {:class "table"}
      [:tr [:th "user"] [:th "song"] [:th "number of time listened"] [:th "normalization"]]
      (for [loc all-locs]
        [:tr [:td (first loc)] [:td (nth loc 1)] [:td ] [:td ]])]])))

;;(all-users-page-datom)

(defn list-user-songs-HTML
  [users]
  (hic-p/html5
   [:h2 "Songs listened to by the user"]
   [:table {:class "table"}
    [:tr [:th "user"] [:th "song"] [:th "number of time listened"] [:th "normalization"]]
    (for [l users]
      [:tr [:td (:iduser l)] [:td (:idsong l)] [:td (:number l)] [:td (:norm l)]])]
   [:br]))


(defn list-similar-users-HTML
  [user-list]
  (hic-p/html5
   [:h2 "Similar users by songs"]
   [:table {:class "table"}
    [:tr [:th "user"] [:th "number of same songs"] [:th "similarity quoeficient"]]
    (for [item user-list]
      [:tr [:td (:iduser item)] [:td (:expr item)] [:td (:similarity item)]])]))


(defn recommended-songs-HTML
  [song-list]
  (hic-p/html5
   [:h2 "Recommended songs"]
   [:table {:class "table"}
    [:tr [:th "song"] [:th "score"]]
    (for [item song-list]
      [:tr [:td (:idsong item)] [:td (:score item)]])]))


(defn list-user-songs
  "Returns an HTML table, populated with songs listened to by the input user"
  [iduser]
  (let [results (db/select-specific-user (iduser :iduser))
        my-list (time (db/list-similar-users (iduser :iduser)))
        ;;data (time (db/recommended-recordset my-list))
        ]
    (str
     (time (list-user-songs-HTML results))
     ;;(time (list-similar-users-HTML my-list))
     (time (recommended-songs-HTML (db/recommended-songs my-list))))))
