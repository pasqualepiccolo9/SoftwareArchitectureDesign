package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;

/**
 * US22 - Comando concreto (ConcreteCommand) per la rimozione di una traccia da una
 * playlist. Incapsula l'azione e i dati necessari ad annullarla.
 *
 * Receiver: {@link Playlist}. Durante l'esecuzione il comando memorizza la
 * posizione originaria della traccia (prima di rimuoverla); l'annullamento la
 * reinserisce esattamente in quella posizione tramite {@code addTrackAt}
 * (criterio di accettazione 2).
 */
public class RemoveTrackFromPlaylistCommand implements Command {

    // Receiver del pattern: la playlist su cui agisce il comando.
    private final Playlist playlist;
    // Parametro dell'azione: la traccia rimossa (e reinserita in fase di undo).
    private final Track track;
    // Stato salvato per il ripristino: posizione originaria della traccia (-1 = non nota).
    private int index = -1;

    /**
     * @param playlist la playlist destinataria dell'operazione (Receiver)
     * @param track la traccia da rimuovere
     * @throws IllegalArgumentException se playlist o track sono null
     */
    public RemoveTrackFromPlaylistCommand(Playlist playlist, Track track) {
        if (playlist == null || track == null) {
            throw new IllegalArgumentException("Playlist e traccia non possono essere null");
        }
        this.playlist = playlist;
        this.track = track;
    }

    @Override
    public void execute() {
        // Memorizza la posizione PRIMA della rimozione, per poterla ripristinare.
        index = playlist.getTracks().indexOf(track);
        playlist.removeTrack(track);
    }

    @Override
    public void unexecute() {
        if (index >= 0) {
            playlist.addTrackAt(index, track);
        } else {
            playlist.addTrack(track);
        }
    }
}
