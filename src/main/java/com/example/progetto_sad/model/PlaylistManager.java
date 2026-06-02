package com.example.progetto_sad.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistManager {

    private final List<Playlist> playlists;
    
    public PlaylistManager() {
        this.playlists = new ArrayList<>();
    }
    
    /**
     * Crea una nuova playlist, la registra nel sistema e la restituisce.
     * * @param name Il nome della playlist da creare.
     * @return L'oggetto Playlist appena creato.
     * @throws IllegalArgumentException se il nome fornito non supera la validazione.
     */
    public Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name);
        playlists.add(playlist);
        return playlist;
    }
    
    /**
     * Rimuove definitivamente una playlist dal sistema.
     * * @param p La playlist da eliminare.
     * @throws IllegalArgumentException se la playlist specificata è nulla o non è registrata nel gestore.
     */
    public void removePlaylist(Playlist p) {
        if (p == null || !playlists.contains(p)) {
            throw new IllegalArgumentException("Impossibile rimuovere: la playlist specificata non esiste.");
        }
        playlists.remove(p);
    }
    
    /**
     * Restituisce l'elenco completo di tutte le playlist attualmente salvate.
     * * @return La lista globale delle playlist dell'utente.
     */
    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }
}
