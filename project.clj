(defproject amsi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [hiccup "1.0.2"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [criterium "0.4.3"]
                 [midje "1.6.3"]
                 [korma "0.4.0"]]
  :plugins [[lein-ring "0.8.13"]
            [lein-kibit "0.1.2"]
            [lein-midje "3.1.3"]]
  :ring {:handler amsi.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
