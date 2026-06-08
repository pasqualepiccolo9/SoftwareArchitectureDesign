package com.example.progetto_sad.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US4 - test per la gestione della collezione tracce (TrackLibrary)
class TrackLibraryTest {

    private TrackLibrary library;

    @BeforeEach
    void setUp() {
        library = new TrackLibrary();
    }

    // US4 - libreria appena creata e' vuota
    @Test
    void emptyLibraryHasNoTracks() {
        assertTrue(library.getTracks().isEmpty());
    }

    // US4 - aggiunta traccia
    @Test
    void addTrackMakesItAvailable() {
        Track track = new Track("Imagine", "John Lennon", "Rock", 1971, null, 240);

        library.addTrack(track);

        assertEquals(1, library.getTracks().size());
        assertEquals(track, library.getTracks().get(0));
    }

    // US4 - caso limite: traccia null rifiutata
    @Test
    void addNullTrackIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> library.addTrack(null));
    }

    // US4 - piu' tracce nella stessa libreria
    @Test
    void libraryCanStoreMultipleTracks() {
        Track first = new Track("Imagine", "John Lennon", "Rock", 1971, null, 240);
        Track second = new Track("Yesterday", "The Beatles", "Pop", 1965, null, 125);

        library.addTrack(first);
        library.addTrack(second);

        assertEquals(2, library.getTracks().size());
    }

    // US4 - rimozione traccia
    @Test
    void removeTrackRemovesItFromLibrary() {
        Track track = new Track("Imagine", "John Lennon", "Rock", 1971, null, 240);
        library.addTrack(track);

        library.removeTrack(track);

        assertTrue(library.getTracks().isEmpty());
    }

    // US4 - getTracks restituisce una copia non modificabile dall'esterno
    @Test
    void getTracksReturnsUnmodifiableCopy() {
        Track track = new Track("Imagine", "John Lennon", "Rock", 1971, null, 240);
        library.addTrack(track);

        assertThrows(UnsupportedOperationException.class,
                () -> library.getTracks().add(new Track("Altro", "Autore", "Pop", 2000, null, 0)));
    }
}
