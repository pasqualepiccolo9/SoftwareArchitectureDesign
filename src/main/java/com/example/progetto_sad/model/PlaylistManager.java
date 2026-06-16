package com.example.progetto_sad.model;

import com.example.progetto_sad.observer.Observer;
import com.example.progetto_sad.observer.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistManager implements Subject {

    private final List<Playlist> playlists;
    private final List<Observer> observers;
    
    public PlaylistManager() {
        this.playlists = new ArrayList<>();
        this.observers = new ArrayList<>();
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
        notifyObservers();
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
        notifyObservers();
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
        notifyObservers();
    }
    
    /**
     * Restituisce l'elenco completo di tutte le playlist attualmente salvate.
     * * @return La lista globale delle playlist dell'utente.
     */
    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    /**
     * US26 - Registra nel sistema una playlist gia' costruita (es. generata automaticamente
     * per anno), notificando gli observer. Il nome deve essere univoco, come per
     * {@link #createPlaylist(String)}.
     *
     * @param playlist la playlist da registrare
     * @throws IllegalArgumentException se la playlist e' null o il suo nome e' gia' presente
     */
    public void registerPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("La playlist non puo' essere null");
        }
        if (existsByName(playlist.getName())) {
            throw new IllegalArgumentException("Esiste gia' una playlist con questo nome");
        }
        playlists.add(playlist);
        notifyObservers();
    }

    /**
     * US26 - Costruisce una nuova Playlist con il nome e le tracce fornite,
     * senza registrarla nel manager. Restituisce null se la lista e' null o vuota.
     * L'aggiunta al manager e il collegamento con la libreria sono delegati al task INT.
     *
     * @param name   nome da assegnare alla playlist
     * @param tracks tracce da inserire, nell'ordine ricevuto
     * @return la playlist costruita, oppure null se tracks e' null o vuota
     */
    public Playlist buildPlaylistFromTracks(String name, List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        Playlist playlist = new Playlist(name);
        for (Track t : tracks) {
            playlist.addTrack(t);
        }
        return playlist;
    }

    @Override
    public void attach(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
