package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;

/**
 * US22 - Comando concreto (ConcreteCommand) per l'aggiunta di una traccia alla
 * libreria. Incapsula l'azione e i dati necessari ad annullarla.
 *
 * Receiver: {@link TrackLibrary}. L'esecuzione delega l'operazione al receiver
 * ({@code library.addTrack}); l'annullamento rimuove la traccia appena aggiunta,
 * riportando la libreria allo stato precedente (criterio di accettazione 1).
 */
public class AddTrackToLibraryCommand implements Command {

    // Receiver del pattern: la libreria su cui agisce il comando.
    private final TrackLibrary library;
    // Parametro dell'azione: la traccia aggiunta (e rimossa in fase di undo).
    private final Track track;

    /**
     * @param library la libreria destinataria dell'operazione (Receiver)
     * @param track la traccia da aggiungere
     * @throws IllegalArgumentException se library o track sono null
     */
    public AddTrackToLibraryCommand(TrackLibrary library, Track track) {
        if (library == null || track == null) {
            throw new IllegalArgumentException("Libreria e traccia non possono essere null");
        }
        this.library = library;
        this.track = track;
    }

    @Override
    public void execute() {
        library.addTrack(track);
    }

    @Override
    public void unexecute() {
        library.removeTrack(track);
    }
}
