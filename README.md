# README

## Serveur de streaming (python)

Nécessite les librairies `zeroc-ice` et `python-vlc`.

`pip install zeroc-ice python-vlc`

Modifier les fichiers contenant les musiques (dans la fonction addTracks).

Générer les fichiers à partir du code Ice:

`slice2py Coll.ice`

Lancer le serveur:

`python server.py`

---

## Analyseur de commandes (python)

Nécessite les libraires suivantes:

`pip install flask flask-jsonpify flask-restful`

Exécution:

`python command_analyzer.py`

---

## Client Android

Nécessite un appareil ou un émulateur Android >= 19.

Exécuter `slice2java Coll.ice`
Placer le dossier 'Vocal' (créé à l'aide de slice) dans Application/app/src/main/java

Modifier le fichier Application/app/main/src/res/values/strings.xml
Remplacer la valeur 'ip' par celle donnée par le serveur de streaming.

Lancer l'application.
