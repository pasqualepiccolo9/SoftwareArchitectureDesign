package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;

/**
 * US22 - Comando concreto (ConcreteCommand) per la creazione di una playlist.
 * Incapsula l'azione e i dati necessari ad annullarla.
 *
 * Receiver: {@link PlaylistManager}. L'esecuzione delega la creazione al receiver
 * ({@code createPlaylist}) e memorizza la playlist creata; l'annullamento la rimuove,
 * riportando il sistema allo stato precedente.
 */
public class CreatePlaylistCommand implements Command {

    // Receiver del pattern: il gestore delle playlist.
    private final PlaylistManager manager;
    // Parametro dell'azione: il nome della playlist da creare.
    private final String name;
    // Stato salvato per il ripristino: la playlist creata, da rimuovere in undo.
    private Playlist created;

    /**
     * @param manager il gestore delle playlist (Receiver)
     * @param name il nome della playlist da creare
     * @throws IllegalArgumentException se manager o name sono null
     */
    public CreatePlaylistCommand(PlaylistManager manager, String name) {
        if (manager == null || name == null) {
            throw new IllegalArgumentException("Manager e nome non possono essere null");
        }
        this.manager = manager;
        this.name = name;
    }

    @Override
    public void execute() {
        created = manager.createPlaylist(name);
    }

    @Override
    public void unexecute() {
        if (created != null) {
            manager.removePlaylist(created);
        }
    }
}
