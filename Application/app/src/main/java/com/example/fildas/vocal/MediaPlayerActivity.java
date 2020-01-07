package com.example.fildas.vocal;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.IOException;

import Vocal.*;

public class MediaPlayerActivity extends VocalActivity {

    private MediaPlayer mediaPlayer;

    private SearchView searchView; // Barre de recherche
    private Track[] tracks; // La liste des morceaux affichés
    private ListView listView; // La View correspondant à la liste des morceaux affichés
    private ArrayAdapter trackAdapter;

    Button startButton;
    Button pauseButton;
    Button stopButton;
    ProgressBar progressBar;

    // Le morceau actuellement joué
    Vocal.Track currentTrack;
    // Indique si le player est actif ou à l'arrêt
    protected boolean pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediaplayer);
        initializeUI();
        pause = false;
        index();
    }

    /**
     * 'Index' de l'activité
     */
    protected void index() {
        showServerCollection();
        searchView.clearFocus();
    }

    /**
     * Affiche la liste des morceaux du serveur
     */
    private void showServerCollection() {
        if (iceProxy != null) {
            tracks = iceProxy.getCollection();
            setList(tracks);
            trackAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Initialise les éléments de l'interface
     */
    protected void initializeUI() {
        // ListView - La liste des morceaux affichés
        listView = (ListView) findViewById(R.id.listView);
        trackAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(tracks[position].title);
                text2.setText(tracks[position].author);
                return view;
            }
        };
        listView.setAdapter(trackAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                streamListItem(position);
            }
        });

        // SearchView - Recherche d'un morceau
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForMusic(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // Start Button - Démarre le premier élément de la liste affichée
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tracks.length > 0)
                    streamListItem(0);
            }
        });

        // Pause button - Arrête ou reprend la musique.
        pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pause)
                    resumeMediaPlayer();
                else
                    pauseMediaPlayer();
            }
        });

        // Stop button - Arrête complètement le player et le streaming
        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMediaPlayer();
            }
        });

        // ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    // Redéfinit la liste des morceaux affichés
    public void setList(Track[] tracks) {
        trackAdapter.clear();
        if (tracks.length == 0) {
            this.tracks = null;
        } else {
            this.tracks = tracks;
            for (Track track : tracks) {
                trackAdapter.add(track.author + " - " + track.title);
            }
        }
        trackAdapter.notifyDataSetChanged();
    }

    // Définit les actions à réaliser en fonction des commandes renvoyées
    // par l'analyseur de requêtes
    @Override
    protected void onVoiceCommandAnalyzed(String command, String params) {
        switch(command) {
            case "play":
                playMusic(params);
                break;
            case "pause":
                pauseMediaPlayer();
                break;
            case "resume":
                resumeMediaPlayer();
                break;
            case "stop":
                stopMediaPlayer();
                break;
            case "search":
                searchView.setQuery(params, true);
                break;
            case "index":
                index();
                break;
            default:
        }
    }

    // Recherche une musique sur le serveur Ice et met à jour la liste affichée
    private void searchForMusic(String params) {
        if (iceProxy != null) {
            Track t = new Track();
            t.search = params;
            tracks = iceProxy.search(t);
            setList(tracks);
        }
        trackAdapter.notifyDataSetChanged();
    }

    // Essaie de démarrer le streaming une musique en fonction d'une recherche
    public void playMusic(String search) {
        System.out.println("Play music: " + search);
        Track t = new Track();
        t.search = search;
        streamTrack(t);
    }

    // Démarre le streaming d'une musique en fonction de son numéro dans la liste affichée
    public void streamListItem(int i) {
        stopMediaPlayer();
        if(mediaPlayer == null) {
            Track t = new Track();
            t.title = tracks[i].title;
            t.author = tracks[i].author;
            streamTrack(t);
        }
    }

    // Essaie de démarrer le streaming sur le serveur en effectuant la recherche d'un morceau,
    // et démarre la lecture d'une musique sur le client.
    public void streamTrack(Track t) {
        if(iceProxy != null) {
            Vocal.Track[] tracks = iceProxy.searchTrackAndStream(t);
            if (tracks.length == 0) {
                setList(null);
            } else if (tracks.length == 1) {
                currentTrack = tracks[0];
                createMediaPlayer();
                startMediaPlayer();
            } else {
                setList(tracks);
            }
            System.out.println("Ice OK!");
        }
    }

    // Initialise le lecteur
    public void createMediaPlayer() {
        stopMediaPlayer();
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        try {
            String streamURL = String.format("http://%1$s:%2$s", getString(R.string.ip), getString(R.string.port_streaming));
            System.out.println(streamURL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(streamURL);
            mediaPlayer.prepare();
            if(currentTrack != null) {
                progressBar.setMax(currentTrack.duration);
                Handler mHandler = new Handler();
                MediaPlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                            progressBar.setProgress(mCurrentPosition);
                        }
                        mHandler.postDelayed(this, 1000);
                    }
                });
            }
        } catch(IOException e) {
            System.out.println("Error setting new media!");
        }
    }

    // Démarre la lecture et met à jour le texte du bouton pause
    public void startMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer.start();
            pause = false;
            pauseButton.setText(R.string.pause);
        }
    }

    // Demande au serveur de reprendre le streaming
    // et reprends la lecture
    public void resumeMediaPlayer() {
        if(pause) {
            if(iceProxy != null)
                iceProxy.resumeStream();
            startMediaPlayer();
        }
    }

    // Demande au serveur d'arrêter le streaming et arrête la lecture
    public void pauseMediaPlayer() {
        if(!pause) {
            if(mediaPlayer != null) {
                mediaPlayer.pause();
                pause = true;
                pauseButton.setText(R.string.resume);
            }
            if(iceProxy != null)
                iceProxy.pauseStream();
        }
    }

    // Arrête la lecture et demande au serveur d'arrêter le streaming
    public void stopMediaPlayer() {
        if(mediaPlayer != null) {
            pause = false;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            if(iceProxy != null)
                iceProxy.stopStream();
            progressBar.setProgress(0);
        }
    }
}
