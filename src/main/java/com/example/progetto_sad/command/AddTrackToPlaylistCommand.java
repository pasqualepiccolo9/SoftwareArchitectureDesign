package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;

/**
 * US22 - Comando concreto (ConcreteCommand) per l'aggiunta di una traccia a una
 * playlist. Incapsula l'azione e i dati necessari ad annullarla.
 *
 * Receiver: {@link Playlist}. L'esecuzione delega l'operazione al receiver
 * ({@code playlist.addTrack}); l'annullamento rimuove la traccia appena aggiunta,
 * riportando la playlist allo stato precedente (criterio di accettazione 1).
 */
public class AddTrackToPlaylistCommand implements Command {

    // Receiver del pattern: la playlist su cui agisce il comando.
    private final Playlist playlist;
    // Parametro dell'azione: la traccia aggiunta (e rimossa in fase di undo).
    private final Track track;

    /**
     * @param playlist la playlist destinataria dell'operazione (Receiver)
     * @param track la traccia da aggiungere
     * @throws IllegalArgumentException se playlist o track sono null
     */
    public AddTrackToPlaylistCommand(Playlist playlist, Track track) {
        if (playlist == null || track == null) {
            throw new IllegalArgumentException("Playlist e traccia non possono essere null");
        }
        this.playlist = playlist;
        this.track = track;
    }

    @Override
    public void execute() {
        playlist.addTrack(track);
    }

    @Override
    public void unexecute() {
        playlist.removeTrack(track);
    }
}
