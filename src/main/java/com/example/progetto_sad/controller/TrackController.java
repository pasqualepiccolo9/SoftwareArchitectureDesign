package com.example.progetto_sad.controller;

import com.example.progetto_sad.factory.TrackFactory;
import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;

import java.util.List;

public class TrackController {

    private final TrackLibrary library;

    public TrackController(TrackLibrary library) {
        this.library = library;
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
        library.addTrack(track);
    }

    // US2 - modifica dati traccia (durata esclusa: e' immutabile, estratta dal file)
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

    // US4 - elenco tracce della libreria
    /**
     * US4 - Restituisce l'elenco (in sola lettura) delle tracce in libreria.
     *
     * @return copia non modificabile delle tracce presenti
     */
    public List<Track> getTracks() {
        return library.getTracks();
    }

    /**
     * US3 - Elimina una traccia dalla libreria.
     *
     * @param t la traccia da eliminare
     * @throws IllegalArgumentException se nessuna traccia e' selezionata (null)
     */
    public void deleteTrack(Track t) {
        if (t == null) {
            throw new IllegalArgumentException("Nessuna traccia selezionata");
        }
        library.removeTrack(t);
    }
}
