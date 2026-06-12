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
     * Il nome deve essere univoco: il confronto ignora gli spazi iniziali/finali e
     * le differenze tra maiuscole e minuscole.
     *
     * @param name Il nome della playlist da creare.
     * @return L'oggetto Playlist appena creato.
     * @throws IllegalArgumentException se il nome e' vuoto o gia' esistente.
     */
    public Playlist createPlaylist(String name) {
        // US5 - il nome deve essere univoco
        if (existsByName(name)) {
            throw new IllegalArgumentException("Esiste gia' una playlist con questo nome");
        }
        Playlist playlist = new Playlist(name);
        playlists.add(playlist);
        return playlist;
    }

    // US5 - verifica se esiste gia' una playlist con lo stesso nome (ignorando spazi
    // iniziali/finali e differenze tra maiuscole/minuscole)
    private boolean existsByName(String name) {
        if (name == null) {
            return false;
        }
        String target = name.trim();
        for (Playlist p : playlists) {
            if (p.getName().trim().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
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
        // US3 - sincronizzazione inversa: ogni traccia non deve piu' riferire questa
        // playlist che sta per essere eliminata (evita riferimenti a playlist fantasma).
        for (Track t : p.getTracks()) {
            t.removePlaylist(p);
        }
        playlists.remove(p);
    }

    /**
     * US22 - Reinserisce una playlist esistente in una posizione specifica,
     * ripristinando la sincronizzazione con le sue tracce. E' l'esatto inverso di
     * {@link #removePlaylist(Playlist)} e serve ai comandi di annullamento per
     * ripristinare una playlist eliminata: poiche' la playlist conserva le proprie
     * tracce anche dopo la rimozione, e' sufficiente ripristinarne i collegamenti.
     *
     * @param index la posizione di inserimento, da 0 a size (inclusi)
     * @param p la playlist da reinserire
     * @throws IllegalArgumentException se p e' null, l'indice e' fuori intervallo o
     *         la playlist e' gia' presente
     */
    public void addPlaylistAt(int index, Playlist p) {
        if (p == null) {
            throw new IllegalArgumentException("La playlist non puo' essere null");
        }
        if (index < 0 || index > playlists.size()) {
            throw new IllegalArgumentException("Posizione di inserimento non valida");
        }
        if (playlists.contains(p)) {
            throw new IllegalArgumentException("La playlist e' gia' presente");
        }
        playlists.add(index, p);
        // US22 - ripristina la sincronizzazione inversa rotta da removePlaylist:
        // ogni traccia torna a riferire questa playlist.
        for (Track t : p.getTracks()) {
            t.addPlaylist(p);
        }
    }
    
    /**
     * Restituisce l'elenco completo di tutte le playlist attualmente salvate.
     * * @return La lista globale delle playlist dell'utente.
     */
    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }
}
