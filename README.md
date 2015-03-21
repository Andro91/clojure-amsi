# amsi

Software Engineering Tools and Methods Class Project
Developed in the Clojure programming language (version 1.6.0) in the LightTable IDE

The original idea was to propose a solution to the MillionSongDataset challenge (http://www.kaggle.com/c/msdchallenge)

SUMMRARU:
Dataset data in the form of triplets [userid, songid, number]
The first two are the id of a user, and the id of a song, both strings;
The last part of a triplet is the number of times the user has listened to a given song;
The goal is to make a prediction on what the next song a users listens to will be, based on the data provided.

The database used in the project is present in an .rar archive format (clojurebase[UPDATE-DATE].rar)
The archive contains the sql DUMP file, exported via the MySQL Workbench utility, which is an exact copy of the database used in development.
The database needs to be imported to the local server (Personal tool of choice: WAMP) prior to running the application.

## Requirements

The app requires [Leiningen] 2.0.0 or later

[leiningen]: https://github.com/technomancy/leiningen

## Running

Running the application requires the local ring server to be started,
by executing the following command in the current working directory (project folder)
of the application.

    lein ring server

If the command is successfully executed, the ring server should be up and running on port 3000 (by default)
The app can then be viewed on http://localhost:3000/
