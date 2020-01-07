#!/usr/bin/env python
#-*- coding: utf-8 -*-

import sys, traceback, Ice
import vlc
import socket
import time
import Vocal

ip = socket.gethostbyname(socket.gethostname())
port = 5005

# Classe correspondant au player vlc
class Player:
    def __init__(self):
        self.instance = vlc.Instance()
        self.player = self.instance.media_player_new()
        self.media = None
        print("Initialized player!")

    # Démarre le streaming d'un fichier audio sur l'adresse IP de la machine
    def start(self, filepath, port):
        self.stop()
        options = ':sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100}:http{mux=mp3,dst=:%s/}' %(str(port))
        self.media = self.instance.media_new(filepath, options)
        self.player.set_media(self.media)
        self.player.play()

    def pause(self):
        if self.media != None:
            self.player.pause()

    def resume(self):
        if self.media != None:
            self.player.pause()

    def stop(self):
        if self.media != None:
            self.player.stop()
            self.player.set_media(None)
            self.media = None


# Classe correspondant au serveur Ice
class CollI(Vocal.Coll):
    # Initialisation
    def __init__(self):
        self.collection = []
        self.player = Player()
        self.ip = socket.gethostbyname(socket.gethostname())
        self.port = 8181
        self.addTracks()
        print("Initialized!")

    # Ajoute un morceau
    def addTrack(self, author, title, filepath, duration):
        track = Vocal.Track()

        track.author = author
        track.title = title
        track.filepath = filepath
        track.duration = int(duration)

        print("Add track: " + track.author + " - " + track.title)
        self.collection.append(track)

    # Cherche un/des morceau(x) dans la collection du serveur.
    # Pour cela, se base sur un objet Track, pouvant contenir des
    # informations dans la champs 'title', 'author', ou 'search' (indéfini).
    def search(self, track, current=None):
        print("Search track: title: '%s', author: '%s', search: '%s'" %(track.title, track.author, track.search))
        c = []
        for t in self.collection:
            if ((track.author and track.author.lower() in t.author.lower()) and \
               (track.title and track.title.lower() in t.title.lower())) or \
               (track.search and (track.search.lower() in t.title.lower() or \
                                  track.search.lower() in t.author.lower())):

                c.append(t)
        return c

    # Reçoit en paramètre un objet Track pouvant contenir des informations incomplètes.
    # Cherche le(s) morceau(x) correspondant dans la collection du serveur et les
    # renvoie sous la forme d'une collection.
    # S'il y a exactement un morceau, démarre le stream.
    def searchTrackAndStream(self, track, current=None):
        c = self.search(track)
        if not c:
            print("No track found...")
        elif len(c) == 1:
            print("Streaming!")
            self.startStream(c[0])
        else:
            print("Several tracks found")
        return c

    def startStream(self, track, current=None):
        self.stopStream()
        dst = str(self.ip)+":"+str(self.port)
        self.player.start(track.filepath, self.port)
        print("Streaming '%s' on %s" %(track.filepath, dst))

    def pauseStream(self, current=None):
        print("Pause Stream!")
        self.player.pause()

    def resumeStream(self, current=None):
        print("Resume Stream!")
        self.player.resume()

    def stopStream(self, current=None):
        print("Stop Stream!")
        self.player.stop()

    # Ajoute les morceaux de base dans la collection du serveur.
    def addTracks(self):
        self.addTrack("Phrenia",
             "Perspectives",
             "D:/Musique/num/86/Rock/Phrenia - Perspectives.mp3",
             236)
        self.addTrack("Marty Friedman, Jean-Ken Johnny, KenKen",
             "The Perfect World",
             "D:/Musique/num/88/Anime/Marty Friedman feat. Jean-Ken Johnny, KenKen _ The Perfect World.mp3",
             236)
        self.addTrack("Dragon Ball GT",
             "Dragon Ball GT Opening",
             "D:/Musique/num/88/Anime/Dragon Ball GT Opening.mp3",
             236)
        self.addTrack("Aico",
             "Incarnation Opening",
             "D:/Musique/num/88/Anime/A.I.C.O. Incarnation Full opening.mp3",
             236)
        self.addTrack("Deuce",
             "Miracle",
             "D:/Musique/num/87/Rock/Deuce/Deuce - Miracle.mp3",
             236)

    # Retourne la collection du serveur
    def getCollection(self, current=None):
        print("getCollection")
        return self.collection


# Initialisation du serveur Ice
with Ice.initialize(sys.argv) as communicator:
    address = "tcp -h %s -p %s" %(ip, port)
    print("IceServer address: %s" %(address))
    adapter = communicator.createObjectAdapterWithEndpoints("SimplePrinterAdapter", address)
    object = CollI()
    adapter.add(object, communicator.stringToIdentity("SimplePrinter"))
    adapter.activate()
    communicator.waitForShutdown()
