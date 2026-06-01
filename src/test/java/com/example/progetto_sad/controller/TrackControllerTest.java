package com.example.progetto_sad.controller;

import com.example.progetto_sad.model.Track;
import com.example.progetto_sad.model.TrackLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// US1 - creazione traccia / US3 - eliminazione traccia
class TrackControllerTest {

    private static final String VALID_PATH = "C:/musica/brano.mp3";
    private static final int DURATION = 180;

    private TrackLibrary library;
    private TrackController controller;

    @BeforeEach
    void setUp() {
        library = new TrackLibrary();
        controller = new TrackController(library);
    }

    // US1
    @Test
    void createTrackAddsItToLibrary() {
        controller.createTrack("Imagine", "John Lennon", "Rock", 1971, VALID_PATH, DURATION);

        assertEquals(1, controller.getTracks().size());
        assertEquals("Imagine", controller.getTracks().get(0).getTitle());
    }

    // US1 - la creazione memorizza TUTTI i campi della traccia, durata inclusa
    @Test
    void createTrackStoresAllFields() {
        controller.createTrack("Imagine", "John Lennon", "Rock", 1971, VALID_PATH, DURATION);

        Track stored = controller.getTracks().get(0);
        assertEquals("Imagine", stored.getTitle());
        assertEquals("John Lennon", stored.getAuthor());
        assertEquals("Rock", stored.getGenre());
        assertEquals(1971, stored.getYear());
        assertEquals(DURATION, stored.getDuration());
    }

    // US1 - input non valido: la traccia non deve essere aggiunta
    @Test
    void createTrackWithInvalidDataDoesNotAddToLibrary() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.createTrack("", "John", "Rock", 1971, VALID_PATH, DURATION));

        assertTrue(controller.getTracks().isEmpty());
    }

    // US3
    @Test
    void deleteTrackRemovesAnExistingTrack() {
        controller.createTrack("Imagine", "John Lennon", "Rock", 1971, VALID_PATH, DURATION);
        Track track = controller.getTracks().get(0);

        controller.deleteTrack(track);

        assertTrue(controller.getTracks().isEmpty());
    }

    // US3 - caso limite: nessuna traccia selezionata
    @Test
    void deleteNullTrackThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.deleteTrack(null));
    }

    // US3 - caso limite: traccia non presente in libreria
    @Test
    void deleteTrackNotInLibraryLeavesLibraryUnchanged() {
        controller.createTrack("Imagine", "John Lennon", "Rock", 1971, VALID_PATH, DURATION);
        Track notStored = new Track("Altro", "Autore", 0, "Pop", 2000);

        assertDoesNotThrow(() -> controller.deleteTrack(notStored));
        assertEquals(1, controller.getTracks().size());
    }
}
