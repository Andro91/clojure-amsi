# amsi

Projekat za predmet Alati i Metode Softverskog Inzenjerstva (AMSI)
Radjen u Clojure programskom jeziku (verzija 1.6.0) u razvojnom okruzenju LightTable

Osnovna ideja je da se da predlog resenja za MillionSongDataset izazov (http://www.kaggle.com/c/msdchallenge)

UKRATKO:
Dataset sadrzi podatke u formatu trojki [userid, songid, number]
Prva dva su id korisnika, i id pesme u String formatu;
Poslednji element trojke je broj koji predstavlja koliko puta je korisnik odslusao datu pesmu;
Poenta je da se na osnovu datih podataka napravi predvidjanje o tome koju ce pesmu sledecu korisnik poslusati.

Uz projekat je dodata i baza u .rar arhivi (clojurebase[DATUM-IZMENE].rar)
U arhivi se nalazi sql DUMP fajl, koji je izvucen (exported) u MySql Workbench, i predstavlja identicnu kopiju baze na mom lokalnom racunaru
Potrebno je bazu uvesti (import) bazu na lokalni server (Preporuka WAMP, uz koriscenje MySql Workbench)

## Pred-zahtevi

Potreban je [Leiningen][] 2.0.0 ili noviji.

[leiningen]: https://github.com/technomancy/leiningen

## Pokretanje

Za pokretanje web servera za aplikaciju, potrebno je u terminalu,
u radnom direktorijumu u kom se nalazi verzija aplikacije, izvrsiti komandu:

    lein ring server

Po uspesno izvrsenoj komandi, podize se server na (po default-u) portu 3000

Aplikaciji se zatim moze pristupiti na http://localhost:3000/
