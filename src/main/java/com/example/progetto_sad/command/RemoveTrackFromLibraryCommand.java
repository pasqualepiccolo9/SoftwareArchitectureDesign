package com.example.progetto_sad.command;

import com.example.progetto_sad.model.Playlist;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * US22 - Comando concreto (ConcreteCommand) per la rimozione di una traccia dalla
 * libreria, con gestione della cascata: come {@code TrackController.deleteTrack},
 * la traccia viene rimossa anche da tutte le playlist che la contengono (evita le
 * "tracce fantasma").
 *
 * Receiver: {@link TrackLibrary} (e, per la cascata, le {@link Playlist} coinvolte).
 * Durante l'esecuzione il comando salva lo stato necessario al ripristino completo
 * (una forma leggera di memento): la posizione in libreria e, per ogni playlist
 * coinvolta, la posizione originaria della traccia. L'annullamento reinserisce la
 * traccia in tutte quelle posizioni, ricostruendo anche i collegamenti
 * bidirezionali Track-Playlist (criterio di accettazione 2).
 */
public class RemoveTrackFromLibraryCommand implements Command {

    // Receiver del pattern: la libreria su cui agisce il comando.
    private final TrackLibrary library;
    // Parametro dell'azione: la traccia rimossa (e ripristinata in fase di undo).
    private final Track track;

    // Stato salvato per il ripristino (popolato in execute, prima della rimozione).
    private int libraryIndex = -1;
    private final List<PlaylistPosition> playlistPositions = new ArrayList<>();

    // Coppia (playlist, indice originario della traccia in quella playlist).
    private static final class PlaylistPosition {
        private final Playlist playlist;
        private final int index;

        private PlaylistPosition(Playlist playlist, int index) {
            this.playlist = playlist;
            this.index = index;
        }
    }

    /**
     * @param library la libreria destinataria dell'operazione (Receiver)
     * @param track la traccia da rimuovere
     * @throws IllegalArgumentException se library o track sono null
     */
    public RemoveTrackFromLibraryCommand(TrackLibrary library, Track track) {
        if (library == null || track == null) {
            throw new IllegalArgumentException("Libreria e traccia non possono essere null");
        }
        this.library = library;
        this.track = track;
    }

    @Override
    public void execute() {
        // 1) Cattura lo stato PRIMA di rimuovere, per poterlo ripristinare nell'undo.
        libraryIndex = library.getTracks().indexOf(track);
        playlistPositions.clear();
        for (Playlist p : track.getPlaylists()) {
            playlistPositions.add(new PlaylistPosition(p, p.getTracks().indexOf(track)));
        }
        // 2) Rimozione a cascata: prima dalle playlist, poi dalla libreria.
        for (PlaylistPosition pp : playlistPositions) {
            pp.playlist.removeTrack(track);
        }
        library.removeTrack(track);
    }

    @Override
    public void unexecute() {
        // Ripristina la traccia in libreria nella posizione originaria.
        // US22 - casi limite: se la traccia e' gia' tornata in libreria si ignora
        // (evita il doppione); se la libreria si e' accorciata e l'indice salvato
        // e' fuori intervallo, si reinserisce nella posizione piu' vicina (in coda).
        if (!library.contains(track)) {
            int librarySize = library.getTracks().size();
            int libraryTarget = (libraryIndex < 0 || libraryIndex > librarySize)
                    ? librarySize : libraryIndex;
            library.addTrackAt(libraryTarget, track);
        }
        // ...e in ogni playlist coinvolta, alla rispettiva posizione originaria
        // (addTrackAt ricostruisce anche il collegamento bidirezionale Track-Playlist).
        // US22 - stessi casi limite per ciascuna playlist: duplicato ignorato,
        // indice fuori intervallo riportato in coda. Cosi' un imprevisto su una
        // playlist non interrompe il ripristino delle altre.
        for (PlaylistPosition pp : playlistPositions) {
            if (pp.playlist.getTracks().contains(track)) {
                continue;
            }
            int size = pp.playlist.getTracks().size();
            int target = (pp.index < 0 || pp.index > size) ? size : pp.index;
            pp.playlist.addTrackAt(target, track);
        }
    }
}
