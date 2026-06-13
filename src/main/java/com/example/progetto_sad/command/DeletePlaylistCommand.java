package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.PlaylistManager;

/**
 * US22 - Comando concreto (ConcreteCommand) per l'eliminazione di una playlist.
 * Incapsula l'azione e i dati necessari ad annullarla.
 *
 * Receiver: {@link PlaylistManager}. Durante l'esecuzione memorizza la posizione
 * originaria della playlist (prima di rimuoverla); l'annullamento la reinserisce in
 * quella posizione tramite {@code addPlaylistAt}, che ripristina anche i collegamenti
 * con le tracce contenute.
 */
public class DeletePlaylistCommand implements Command {

    // Receiver del pattern: il gestore delle playlist.
    private final PlaylistManager manager;
    // Parametro dell'azione: la playlist rimossa (e ripristinata in fase di undo).
    private final Playlist playlist;
    // Stato salvato per il ripristino: posizione originaria nel gestore (-1 = non nota).
    private int index = -1;

    /**
     * @param manager il gestore delle playlist (Receiver)
     * @param playlist la playlist da eliminare
     * @throws IllegalArgumentException se manager o playlist sono null
     */
    public DeletePlaylistCommand(PlaylistManager manager, Playlist playlist) {
        if (manager == null || playlist == null) {
            throw new IllegalArgumentException("Manager e playlist non possono essere null");
        }
        this.manager = manager;
        this.playlist = playlist;
    }

    @Override
    public void execute() {
        // Memorizza la posizione PRIMA della rimozione, per poterla ripristinare.
        index = manager.getPlaylists().indexOf(playlist);
        manager.removePlaylist(playlist);
    }

    @Override
    public void unexecute() {
        // US22 - caso limite: se la playlist e' gia' tornata nel gestore (es.
        // ripristinata da un'altra operazione), un nuovo inserimento lancerebbe
        // per duplicato: si ignora.
        if (manager.getPlaylists().contains(playlist)) {
            return;
        }
        // US22 - caso limite: se nel frattempo il gestore si e' accorciato, la
        // posizione salvata puo' essere fuori intervallo; si reinserisce nella
        // posizione piu' vicina possibile (in coda) invece di lanciare.
        int size = manager.getPlaylists().size();
        int target = (index < 0 || index > size) ? size : index;
        manager.addPlaylistAt(target, playlist);
    }
}
