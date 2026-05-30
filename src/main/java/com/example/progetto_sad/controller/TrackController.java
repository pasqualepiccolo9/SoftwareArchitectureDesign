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

    // US1 - creazione e caricamento traccia
    public void createTrack(String title, String author, String genre, int year, String filePath) {
        Track track = TrackFactory.createTrack(title, author, genre, year, filePath);
        library.addTrack(track);
    }

    // US2 - modifica dati traccia
    public void updateTrack(Track t, String title, String author, String genre, int year, int duration) {
        if (t == null) {
            throw new IllegalArgumentException("Nessuna traccia selezionata");
        }
        if (!library.contains(t)) {
            throw new IllegalArgumentException("Traccia non presente in libreria");
        }
        TrackFactory.validateMetadata(title, author, genre, year);
        TrackFactory.validateDuration(duration);
        t.setTitle(title.trim());
        t.setAuthor(author.trim());
        t.setGenre(genre.trim());
        t.setYear(year);
        t.setDuration(duration);
        library.trackUpdated();
    }

    // US4 - elenco tracce della libreria
    public List<Track> getTracks() {
        return library.getTracks();
    }

    // US3 - eliminazione traccia
    public void deleteTrack(Track t) {
        if (t == null) {
            // US3 - gestione del caso "nessuna traccia selezionata"
            throw new IllegalArgumentException("Nessuna traccia selezionata");
        }
        library.removeTrack(t);
    }
}
