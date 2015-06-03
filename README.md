# amsi

Software Engineering Tools and Methods Class Project
Developed in the Clojure programming language (version 1.6.0) in the LightTable IDE

The original idea was to propose a solution to the MillionSongDataset challenge (http://www.kaggle.com/c/msdchallenge)

### SUMMARY:
Dataset data in the form of triplets [userid, songid, number]
The first two are the id of a user, and the id of a song, both strings;
The last part of a triplet is the number of times the user has listened to a given song;
The goal is to make a prediction on what the next song a users listens to will be, based on the data provided.

### NORMALISATION
Since the number of times a user can listen to a song can be virtually limitless, a normalisation method
(feature scaling) is used in order to have the data range between two values
(5 and 10, 10 being the highest user appeal for a song).

The database used in the project is present in an .rar archive format (clojure.rar)
The archive contains the SQLLite database.

## User manual
Home page is meant to contain the instructions for using the application, but it's still incomplete.
The "select user" page is the main part of the application. The textbox expects a userid as an input, and after submiting the id,
the system evaluates the user, and recommends songs for him to listen to. The output is ina the form of three tables, first being
the list of songs the user has listened to, second, the list of similar users, and finally, the list of system-recommended songs.
The "all users" page displays the triplets from the database, limited to the first 1000 records (for processing purposes).

## Requirements

The application requires [Leiningen] 2.0.0 or later

[leiningen]: https://github.com/technomancy/leiningen

## Running

Running the application requires the local ring server to be started,
by executing the following command in the current working directory (project folder)
of the application.

    lein ring server

If the command is successfully executed, the ring server should be up and running on port 3000 (by default)
The app can then be viewed on http://localhost:3000/

## UPDATE

Project profiling data (tool used - jvisualvm)

![alt tag](http://s15.postimg.org/9yz9ytkff/profiling.png)


Query results from SQLLite studio
![alt tag](http://oi61.tinypic.com/1zw0y0o.jpg)
