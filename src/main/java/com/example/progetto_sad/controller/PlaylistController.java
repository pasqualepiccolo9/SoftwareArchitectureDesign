package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.Track;

import java.util.Collections;
import java.util.List;

/**
 * Controller responsabile della gestione delle azioni dell'utente relative alle playlist.
 * Delega le operazioni di business al livello Model.
 */
public class PlaylistController {

    private final PlaylistManager manager;

    /**
     * Crea un controller che utilizza il gestore delle playlist specificato.
     *
     * @param manager il gestore usato per creare e rimuovere playlist
     */
    public PlaylistController(PlaylistManager manager) {
        this.manager = manager;
    }

    /* US5 - creazione playlist */

    /**
     * Crea una nuova playlist con il nome specificato.
     *
     * @param name il nome della playlist da creare
    */
    public void createPlaylist(String name) {
        try {
            manager.createPlaylist(name);
            System.out.println("Playlist '" + name + "' creata con successo.");
        } catch (IllegalArgumentException e) {
            System.err.println("Errore UI: " + e.getMessage());
        }
    }

    /**
     * Rimuove la playlist specificata.
     *
     * @param playlist la playlist da rimuovere
     */
    public void removePlaylist(Playlist playlist) {
        if (playlist != null) {
            manager.removePlaylist(playlist);
        }
    }

   /* US6 - aggiunta traccia a playlist */

    /**
     * Aggiunge una traccia alla playlist specificata.
     *
     * @param track la traccia da aggiungere
     * @param playlist la playlist a cui aggiungere la traccia
     */
    public void addTrackToPlaylist(Track track, Playlist playlist) {
        if (playlist != null) {
            playlist.addTrack(track);
        }
    }

    /* US7 - rimozione traccia da playlist */

    /**
     * Rimuove una traccia dalla playlist specificata.
     *
     * @param track la traccia da rimuovere
     * @param playlist la playlist da cui rimuovere la traccia
     */
    public void removeTrackFromPlaylist(Track track, Playlist playlist) {
        if (playlist != null) {
            playlist.removeTrack(track);
        }
    }

    /* US8 - visualizzazione contenuto playlist */
    /**
     * Restituisce le tracce contenute nella playlist specificata.
     * Se la playlist è nulla, viene restituita una lista vuota.
     *
     * @param playlist la playlist di cui visualizzare il contenuto
     * @return la lista delle tracce contenute nella playlist, oppure una lista vuota se la playlist è nulla
     */
    public List<Track> getPlaylistTracks(Playlist playlist) {
        if (playlist == null) {
            return Collections.emptyList();
        }
        return playlist.getTracks();
    }

    /**
     * Verifica se la playlist selezionata è nulla o non contiene tracce.
     *
     * @param playlist la playlist selezionata
     * @return true se la playlist è nulla o vuota, false altrimenti
     */
    public boolean isPlaylistEmpty(Playlist playlist) {
        return playlist == null || playlist.getTracks().isEmpty();
    }
}
