(ns amsi.views
  (:require [amsi.db :as db]
            [clojure.string :as str]
            [hiccup.page :as hic-p]))

(defn gen-page-head
  [title]
  [:head
   [:title title]
   (hic-p/include-css "/bootstrap.min.css")
   (hic-p/include-js "https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js")
   (hic-p/include-js "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js")
   (hic-p/include-css "/style.css")
   ])

(def navbar
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
  []
  (hic-p/html5
   (gen-page-head "Home")
   navbar
   [:div {:class "container"}
   [:h1 "Home"]
   [:p "Wellcome to Clojure web app. Further description to follow."]]))

(defn select-user-page
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
    [:script
     (str "$('div#submituser').click(function(){
            var data = $('input#iduser').val();
            $.ajax({
                url: 'ajaxcall',
                type: 'POST',
                data: {iduser: data},
                beforeSend: function(){
                   $('div#loaderdiv').show();
                },
                success: function(result){
                $('div#loaderdiv').hide();
                $('#div1').html(result);
                }});
    });")]))


(defn all-users-page
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
