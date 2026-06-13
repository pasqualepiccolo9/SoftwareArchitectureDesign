package com.example.progetto_sad.controller;

import com.example.progetto_sad.command.AddTrackToLibraryCommand;
import com.example.progetto_sad.command.CommandManager;
import com.example.progetto_sad.command.RemoveTrackFromLibraryCommand;
import com.example.progetto_sad.factory.TrackFactory;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;

import java.util.List;

/**
 * Controller applicativo per la gestione delle tracce nella libreria.
 * Delega validazione e creazione a {@link TrackFactory}; notifica la UI
 * tramite {@link TrackLibrary} (pattern Observer).
 *
 * US22 - Nel pattern Command questo controller e' il Client: crea e configura i
 * comandi concreti (aggiunta/eliminazione traccia) e li passa all'Invoker
 * ({@link CommandManager}), che li esegue e li registra nello storico per
 * l'annullamento. Le firme dei metodi invocati dalla UI restano invariate.
 */
public class TrackController {

    private final TrackLibrary library;

    // US22 - Invoker condiviso a cui il controller (Client) consegna i comandi.
    private final CommandManager commandManager;

    /**
     * Crea il controller con un proprio storico comandi. Mantiene compatibile
     * l'uso esistente (test e anteprime); l'applicazione reale usa il costruttore
     * a due argomenti per condividere un unico storico tra i controller.
     *
     * @param library libreria in cui vengono aggiunte, modificate ed eliminate le tracce
     */
    public TrackController(TrackLibrary library) {
        this(library, new CommandManager());
    }

    /**
     * US22 - Crea il controller collegandolo allo storico comandi condiviso
     * dell'applicazione, cosi' che il pulsante "Annulla" agisca su un'unica
     * cronologia per tutte le operazioni annullabili.
     *
     * @param library libreria in cui vengono aggiunte, modificate ed eliminate le tracce
     * @param commandManager l'Invoker condiviso che esegue e registra i comandi
     * @throws IllegalArgumentException se commandManager e' null
     */
    public TrackController(TrackLibrary library, CommandManager commandManager) {
        if (commandManager == null) {
            throw new IllegalArgumentException("Il command manager non puo' essere null");
        }
        this.library = library;
        this.commandManager = commandManager;
    }

    /**
     * US1 - Crea una nuova traccia dai dati del form (con la durata gia' estratta
     * dal file) e la aggiunge alla libreria.
     *
     * @param title           titolo
     * @param author          autore
     * @param genre           genere
     * @param year            anno di pubblicazione
     * @param filePath        percorso del file audio
     * @param durationSeconds durata in secondi gia' estratta dal file
     * @throws IllegalArgumentException se i dati non sono validi
     */
    public void createTrack(String title, String author, String genre, int year,
                            String filePath, int durationSeconds) {
        Track track = TrackFactory.createTrack(title, author, genre, year, filePath, durationSeconds);
        // US22 - l'aggiunta transita dall'Invoker come comando annullabile; se la
        // validazione della factory fallisce, nessun comando entra nello storico.
        commandManager.executeCommand(new AddTrackToLibraryCommand(library, track));
    }

    /**
     * US2 - Aggiorna titolo, autore, genere e anno di una traccia gia' in libreria.
     * La durata non viene modificata (immutabile, estratta dal file alla creazione).
     * Dopo la modifica notifica gli observer tramite {@link TrackLibrary#trackUpdated()}.
     *
     * @param t      traccia da aggiornare (deve essere presente in libreria)
     * @param title  nuovo titolo
     * @param author nuovo autore
     * @param genre  nuovo genere
     * @param year   nuovo anno di pubblicazione
     * @throws IllegalArgumentException se la traccia e' null, non e' in libreria o i dati non sono validi
     */
    public void updateTrack(Track t, String title, String author, String genre, int year) {
        if (t == null) {
            throw new IllegalArgumentException("Nessuna traccia selezionata");
        }
        if (!library.contains(t)) {
            throw new IllegalArgumentException("Traccia non presente in libreria");
        }
        TrackFactory.validateMetadata(title, author, genre, year);
        t.setTitle(title.trim());
        t.setAuthor(author.trim());
        t.setGenre(genre.trim());
        t.setYear(year);
        library.trackUpdated();
    }

    /**
     * US4 - Restituisce l'elenco (in sola lettura) delle tracce in libreria.
     *
     * @return copia non modificabile delle tracce presenti
     */
    public List<Track> getTracks() {
        return library.getTracks();
    }

    /**
     * US3 / US22 - Elimina una traccia dalla libreria tramite un comando annullabile.
     * La rimozione a cascata da tutte le playlist (evita le "tracce fantasma") e il
     * salvataggio delle posizioni originarie per l'undo sono incapsulati nel comando
     * {@link RemoveTrackFromLibraryCommand}.
     *
     * Se la traccia non e' presente in libreria l'operazione resta tollerante come in
     * passato (nessuna eccezione), ma non transita dall'Invoker: un'eliminazione senza
     * effetto non deve entrare nello storico delle operazioni annullabili.
     *
     * @param t la traccia da eliminare
     * @throws IllegalArgumentException se nessuna traccia e' selezionata (null)
     */
    public void deleteTrack(Track t) {
        if (t == null) {
            throw new IllegalArgumentException("Nessuna traccia selezionata");
        }
        if (!library.contains(t)) {
            return;
        }
        commandManager.executeCommand(new RemoveTrackFromLibraryCommand(library, t));
    }
}
