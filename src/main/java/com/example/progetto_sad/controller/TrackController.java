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

    // US2 - modifica dati traccia (assegnata ad altro membro)
    public void updateTrack(Track t, String title, String author, String genre, int year) {
        // TODO US2: validare i nuovi dati e aggiornare la traccia esistente nella libreria.
        throw new UnsupportedOperationException("US2 non ancora implementata");
    }

    // US4 - elenco tracce della libreria (assegnata ad altro membro per la vista)
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
