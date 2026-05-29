package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;

import java.util.List;

public class PlaylistController {

    private final PlaylistManager manager;

    public PlaylistController(PlaylistManager manager) {
        this.manager = manager;
    }

    // US5 - creazione playlist
    public void createPlaylist(String name) {
        // TODO US5: validare il nome e creare la playlist tramite manager.createPlaylist(name).
        throw new UnsupportedOperationException("US5 non ancora implementata");
    }

    // US6 - aggiunta traccia a playlist
    public void addTrackToPlaylist(Track t, Playlist p) {
        // TODO US6: gestire duplicati/selezioni non valide, poi p.addTrack(t).
        throw new UnsupportedOperationException("US6 non ancora implementata");
    }

    // US7 - rimozione traccia da playlist
    public void removeTrackFromPlaylist(Track t, Playlist p) {
        // TODO US7: rimuovere la traccia dalla playlist senza eliminarla dalla libreria.
        throw new UnsupportedOperationException("US7 non ancora implementata");
    }

    // US8 - visualizzazione contenuto playlist
    public List<Track> getPlaylistTracks(Playlist p) {
        // TODO US8: restituire le tracce della playlist, gestendo i casi playlist vuota/nulla.
        throw new UnsupportedOperationException("US8 non ancora implementata");
    }
}
