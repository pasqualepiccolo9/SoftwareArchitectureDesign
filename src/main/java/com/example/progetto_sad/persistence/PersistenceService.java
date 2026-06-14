package com.example.progetto_sad.persistence;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;
import com.example.progetto_sad.model.TrackLibrary;
import com.example.progetto_sad.observer.Observer;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Servizio applicativo per il salvataggio automatico dei dati.
 *
 * Osserva libreria, gestore playlist e playlist esistenti; quando una struttura
 * cambia, delega il salvataggio al repository JSON.
 */
public class PersistenceService implements Observer {

    private final TrackLibrary library;
    private final PlaylistManager playlistManager;
    private final JsonDataRepository repository;
    private final Set<Playlist> observedPlaylists;
    private boolean active;

    public PersistenceService(TrackLibrary library, PlaylistManager playlistManager,
                              JsonDataRepository repository) {
        if (library == null || playlistManager == null || repository == null) {
            throw new IllegalArgumentException("Dipendenze persistenza non valide");
        }
        this.library = library;
        this.playlistManager = playlistManager;
        this.repository = repository;
        this.observedPlaylists = Collections.newSetFromMap(new IdentityHashMap<>());
        this.active = false;
    }

    /**
     * Attiva l'autosalvataggio sulle strutture dati correnti.
     */
    public void startAutoSave() {
        if (active) {
            return;
        }
        library.attach(this);
        playlistManager.attach(this);
        refreshPlaylistObservers();
        active = true;
    }

    /**
     * Disattiva l'autosalvataggio e sgancia gli observer registrati.
     */
    public void stopAutoSave() {
        if (!active) {
            return;
        }
        library.detach(this);
        playlistManager.detach(this);
        for (Playlist playlist : observedPlaylists) {
            playlist.detach(this);
        }
        observedPlaylists.clear();
        active = false;
    }

    /**
     * Salva immediatamente lo stato corrente. Gli errori di scrittura non devono
     * propagarsi alla UI: vengono segnalati su standard error e l'app continua.
     */
    public void saveNow() {
        refreshPlaylistObservers();
        try {
            repository.save(library, playlistManager);
        } catch (IOException e) {
            System.err.println("Salvataggio dati non riuscito: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        saveNow();
    }

    private void refreshPlaylistObservers() {
        Set<Playlist> current = Collections.newSetFromMap(new IdentityHashMap<>());
        current.addAll(playlistManager.getPlaylists());

        for (Playlist playlist : observedPlaylists.toArray(new Playlist[0])) {
            if (!current.contains(playlist)) {
                playlist.detach(this);
                observedPlaylists.remove(playlist);
            }
        }

        for (Playlist playlist : current) {
            if (!observedPlaylists.contains(playlist)) {
                playlist.attach(this);
                observedPlaylists.add(playlist);
            }
        }
    }
}
