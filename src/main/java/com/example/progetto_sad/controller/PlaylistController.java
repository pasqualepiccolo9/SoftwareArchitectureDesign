package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;

import java.util.Collections;
import java.util.List;

public class PlaylistController {

    private final PlaylistManager manager;

    public PlaylistController(PlaylistManager manager) {
        this.manager = manager;
    }

    // US5 - creazione playlist
   public void createPlaylist(String name) {
        try {
            manager.createPlaylist(name);
            System.out.println("Playlist '" + name + "' creata con successo.");
        } catch (IllegalArgumentException e) {
            System.err.println("Errore UI: " + e.getMessage());
        }
    }
   
   public void removePlaylist(Playlist p) {
        if (p != null) {
            manager.removePlaylist(p);
        }
    }

   // US6 - aggiunta traccia a playlist
    public void addTrackToPlaylist(Track t, Playlist p) {
        if (p != null) {
            p.addTrack(t); 
        }
    }

    // US7 - rimozione traccia da playlist
    public void removeTrackFromPlaylist(Track t, Playlist p) {
        if (p != null) {
            p.removeTrack(t);
        }
    }

    // US8 - visualizzazione contenuto playlist
    /**
     * Returns the ordered list of tracks contained in the selected playlist.
     * If the playlist is null, an empty list is returned.
     *
     * @param p the selected playlist
     * @return the list of tracks contained in the playlist, or an empty list if the playlist is null
     */
    public List<Track> getPlaylistTracks(Playlist p) {
        if (p == null) {
            return Collections.emptyList();
        }
        return p.getTracks();
    }

    /**
     * Checks whether the selected playlist is null or contains no tracks.
     *
     * @param p the selected playlist
     * @return true if the playlist is null or empty, false otherwise
     */
    public boolean isPlaylistEmpty(Playlist p) {
        return p == null || p.getTracks().isEmpty();
    }
}
